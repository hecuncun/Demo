def loadValuesYaml(x){
  def valuesYaml = readYaml (file: 'config.yaml')
  return valuesYaml[x];
}
pipeline {
      //agent节点   多个构建从节点   有的只配置了Android环境用于执行Android项目构建，有的只能执行iOS项目构建，有的是用于执行Go项目
      //那这么多不同的节点怎么管理及分配呢？
      //那就是通过对节点声明不同的标签label，然后在我们的构建中指定标签，这样Jenkins就会找到有对应标签的节点去执行构建了
      //agent { label 'Android'}
//                                   build.module: chk
//                                   #google   huawei
//                                   market: Google
//                                   #Debug   Release
//                                   buildType: Debug
//                                   #product(正式环境) stage(灰度环境) test(测试环境)
//                                   build.environment: test
      agent any
      options {//超时了，就会终止这次的构建  options还有其他配置，比如失败后重试整个pipeline的次数：retry(3)
         timeout(time: 1, unit: 'HOURS')
      }
      environment{//一组全局的环境变量键值对  用在stages 使用在“调用方式为${MARKET}”  注意只能在“ ”中识别
         BUILD_MODULE = loadValuesYaml('build.module')
         MARKET = loadValuesYaml('market')
         BUILD_TYPE = loadValuesYaml('buildType')
         BUILD_ENVIRONMENT = loadValuesYaml('build.environment')
      }
      stages {//这里我们已经有默认的检出代码了  开始执行构建和发布
        //可以根据分支配置构建参数   最好的方式时从一个yaml文件中获取对应的配置文件
         stage('readYaml'){
            steps{
                script{
                 println BUILD_MODULE
                 println MARKET
                 println BUILD_TYPE
                 println BUILD_ENVIRONMENT
                 println env.APP_NAME   //在jenkins 配置的全局变量展示
                 println env.IS_JENKINS
                }
             }
        }

//         stage('参数传递给gradle任务'){
//           steps{
//            sh "chmod +x gradlew"
//            sh """
//                  ./gradlew -DfirstParam=${env.APP_NAME} -DsecondParam=${env.KEY} -DthirdParam=${env.PWD} -DisJenkinsParam=${env.IS_JENKINS} -DbuildModule=${env.BUILD_MODULE}
//            sh """
//           }
//         }

//         stage('set local properties'){
//           steps{
//               script{
//                  	   editFile = load env.WORKSPACE + "/editFile.groovy"
//                  	   config_file = env.WORKSPACE + "/local.properties"
//                  	   try{
//                  	       editFile.setKeyValue("market", "${MARKET}", config_file)
//                  	       editFile.setKeyValue("build.module", "${BUILD_MODULE}", config_file)
//                  	       editFile.setKeyValue("build.environment", "${BUILD_ENVIRONMENT}", config_file)
//                  	       editFile.setKeyValue("compileSensorsSdk", "${COMPILE_SENSORS_SDK}", config_file)
//                  	       file_content = readFile config_file
//                            println file_content
//                  	       }catch (Exception e) {
//                  	           error("Error editFile :" + e)
//                  	       }
//               }
//           }
//         }

//         stage('Build master APK') {
//             when{
//                    branch 'master'
//                 }
//             steps {
//                sh "chmod +x gradlew"
//                sh "./gradlew clean assemble${MARKET}${BUILD_TYPE}"
//              }
//             post {
//                 failure {
//                     echo "Build master APK Failure!"
//                 }
//                 success {
//                     echo "Build master APK Success!"
//                 }
//             }
//         }


        stage('Build dev-hcc APK') {
             when{
                 branch 'dev-hcc'
             }
             steps {
                  sh "chmod +x gradlew"
                   sh """
                                   ./gradlew -DfirstParam=${env.APP_NAME} -DsecondParam=${env.KEY} -DthirdParam=${env.PWD} -DisJenkinsParam=${env.IS_JENKINS} -DbuildModule=${env.BUILD_MODULE}
                   sh """
                  sh "./gradlew clean assemble${MARKET}${BUILD_TYPE}"
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

       stage('Build dev-test APK') {
           when{
                branch 'dev-test'
           }
           steps {
                  sh "chmod +x gradlew"
                   sh """
                                   ./gradlew -DfirstParam=${env.APP_NAME} -DsecondParam=${env.KEY} -DthirdParam=${env.PWD} -DisJenkinsParam=${env.IS_JENKINS} -DbuildModule=${env.BUILD_MODULE}
                   sh """
                   sh "./gradlew clean assemble${MARKET}${BUILD_TYPE}"
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

       stage('ArchiveAPK') {//存储的apk
             when{
                   branch 'dev-test'
             }

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
          when{
              branch 'dev-test'
          }
          steps{
                sh "chmod +x gradlew"
                sh './gradlew apkToFir'
          }
          post {
                  failure {
                      echo "Publish Failure!"
                  }
                  success {
                        echo "Publish Success!"
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