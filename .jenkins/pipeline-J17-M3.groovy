pipeline {
    agent any
    tools { 
        maven 'Maven3' 
        jdk 'Java17' 
    }
    stages {
        stage ('build') {
            steps {
                withMaven(
                     // Maven installation declared in the Jenkins "Global Tool Configuration"
                     maven: 'Maven3',

                     // Use `$WORKSPACE/.repository` for local repository folder to avoid shared repositories
                     mavenLocalRepo: '.repository',
                ) {
				    script {
				        if (isUnix()) {
    			            sh 'mvn -fpom.xml -Pjacoco clean install'
					    } else {
    			            bat 'mvn.cmd -fpom.xml -Pjacoco clean install'
					    }
					}
                }
            }
        }
		stage ('collect profiler data') {
		    steps {
                step( [ $class: 'JacocoPublisher' ] )
			}
		}
    }
}
