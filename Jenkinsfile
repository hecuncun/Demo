
node {
  setting = readYaml file: 'config.yaml'
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

      stages {//这里我们已经有默认的检出代码了  开始执行构建和发布
        //可以根据分支配置构建参数   最好的方式时从一个json文件中获取对应的配置文件。再设置给构建脚本的local
         stage('read-yaml'){
            steps{
                script{
                       echo ${setting.market}
                       println setting.getClass()
                }

             }


        }
        stage('Build master APK') {

            when {
                branch 'master'
            }
            steps {
              bat './gradlew clean assembleGoogleRelease'
            }
            post {
              //always 总是运行，无论成功、失败还是其他状态。
              //changed 当前状态与上一次构建状态不同时就运行
              //failure 当前失败时才运行
              //success 当前成功时运行
              // unstable 不稳定状态时运行
              // aborted 被终止时运行。

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
                bat './gradlew clean assembleGoogleRelease'
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

        stage('Upload') {//需执行上传任务   此处配置  fir.im脚本
            steps {
                archiveArtifacts(artifacts: 'app/build/outputs/apk/**/*.apk', fingerprint: true, onlyIfSuccessful: true)
            }
            post {
                failure {
                    echo "Archive Failure!"
                   // emailext body: 'apk版本信息为1.0', subject: 'apk上传成功啦', to: '13753638431@163.com'
                }
                success {
                    echo "Archive Success!"
                  //  emailext body: 'apk版本信息为1.0', subject: 'apk上传成功啦', to: '13753638431@163.com'
                }
            }
        }

        stage('Report') {
            steps {
                echo getChangeString()
            }
        }

        stage('Publish'){
          steps{
             bat './gradlew debugToFir'
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