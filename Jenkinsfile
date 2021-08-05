def loadValuesYaml(x){
  def valuesYaml = readYaml (file: 'config.yaml')
  return valuesYaml[x];
}
pipeline {
      //agent节点   多个构建从节点   有的只配置了Android环境用于执行Android项目构建，有的只能执行iOS项目构建，有的是用于执行Go项目
      //那这么多不同的节点怎么管理及分配呢？
      //那就是通过对节点声明不同的标签label，然后在我们的构建中指定标签，这样Jenkins就会找到有对应标签的节点去执行构建了
      //agent { label 'Android'}
      agent any
      options {//超时了，就会终止这次的构建  options还有其他配置，比如失败后重试整个pipeline的次数：retry(3)
        timeout(time: 1, unit: 'HOURS')
      }
      environment{//一组全局的环境变量键值对  用在stages 使用在“调用方式为${MARKET}”  注意只能在“ ”中识别
         MARKET = loadValuesYaml('market')
         BUILD_TYPE = loadValuesYaml('buildType')
      }
      stages {//这里我们已经有默认的检出代码了  开始执行构建和发布
        //可以根据分支配置构建参数   最好的方式时从一个yaml文件中获取对应的配置文件
         stage('readYaml'){
            steps{
                script{
                 println MARKET
                 println BUILD_TYPE
                }
             }
        }

        stage('set local properties'){
          steps{
              script{
                 	   selenium_test = load env.WORKSPACE + "selenium.groovy"
                 	   config_file = env.WORKSPACE + "local.properties"
                 	   try{
                 	       selenium_test.setKeyValue2("build.module", "nc", config_file)
                 	       file_content = readFile config_file
                            println file_content
                 	       }catch (Exception e) {
                 	           error("Error met:" + e)
                 	        }
              }
          }
        }

        stage('Build master APK') {
            when {
                branch 'master'
            }
            steps {
              bat "./gradlew clean assemble${MARKET}${BUILD_TYPE}"
            }
            post {
                failure {
                    echo "Build master APK Failure!"
                }
                success {
                    echo "Build master APK Success!"
                }
            }
        }

        stage('Build dev APK') {
            when {
                branch 'dev-hcc'
            }
            steps {
                bat "./gradlew clean assemble${MARKET}${BUILD_TYPE}"
            }
            post {
                failure {
                    echo "Build dev APK Failure!"
                }
                success {
                    echo "Build dev APK Success!"

                }
            }
        }

        stage('ArchiveAPK') {//存储的apk
            steps {
                archiveArtifacts(artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true, onlyIfSuccessful: true)
            }
            post {
                failure {
                    echo "Archive Failure!"
                }
                success {
                    echo "Archive Success!"
                }
            }
        }

        stage('Report') {//显示提交信息
            steps {
                echo getChangeString()
            }
        }

        stage('Publish'){//发布fir.im
          steps{
            bat './gradlew apkToFir'
          }
          post {
             failure {
                echo "Publish Failure!"
             }
             success {
                 echo "Publish Success!"
                 emailext body: 'apk版本有更新', subject: 'apk上传成功啦', to: '13753638431@163.com'
             }
          }
        }
    }
}
//report 提交日志
@NonCPS
def getChangeString() {
    MAX_MSG_LEN = 100
    def changeString = ""

    echo "Gathering SCM Changes..."
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            truncated_msg = entry.msg.take(MAX_MSG_LEN)
            changeString += "[${entry.author}] ${truncated_msg}\n"
        }
    }

    if (!changeString) {
        changeString = " - No Changes -"
    }
    return changeString
}