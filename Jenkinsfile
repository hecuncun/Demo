pipeline {
    agent any
      options {
        timeout(time: 1, unit: 'HOURS')
      }
    stages {

        stage('Build master APK') {

            when {
                branch 'master'
            }
            steps {
              bat './gradlew clean assembleRelease'
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
                bat './gradlew clean assembleDebug'

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

        stage('Upload') {
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

        stage('Report') {
            steps {
                echo getChangeString()
            }
        }
    }
}

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