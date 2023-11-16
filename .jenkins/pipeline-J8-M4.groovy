pipeline {
    agent any
    tools { 
        maven 'Maven4'
        jdk 'Java8' 
    }
    stages {
        stage ('build') {
            steps {
                withMaven(
                     // Maven installation declared in the Jenkins "Global Tool Configuration"
                     maven: 'Maven4',

                     // Use `$WORKSPACE/.repository` for local repository folder to avoid shared repositories
                     mavenLocalRepo: '.repository',
                ) {
				    script {
				        if (isUnix()) {
				            sh 'mvn --version'
    			            sh 'mvn -fpom.xml -Pjacoco clean install'
					    } else {
					        bat 'mvn.cmd --version'
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
