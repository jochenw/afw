pipeline {
    agent any
    tools { 
        maven 'Maven3' 
        jdk 'Java8' 
    }
    stages {
        stage ('build') {
            steps {
                withMaven(
                     // Maven installation declared in the Jenkins "Global Tool Configuration"
                     maven: 'Maven3',

                     // Use `$WORKSPACE/.repository` for local repository folder to avoid shared repositories
                     mavenLocalRepo: '.repository',

					 // Additional diagnostic output
					 traceability: true
                ) {
				    script {
				        if (isUnix()) {
    			            sh 'mvn -fpom.xml -Pjacoco clean install javadoc:javadoc'
					    } else {
    			            bat 'mvn.cmd -fpom.xml -Pjacoco clean install javadoc:javadoc'
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
		stage ('Collect JavaDocs') {
		    steps {
		        javadoc(javadocDir: '**/apidocs', keepAll: false)
		    }
		}
    }
}
