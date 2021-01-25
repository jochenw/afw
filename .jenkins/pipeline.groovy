pipeline {
    agent any
    tools { 
        maven 'Maven3' 
        jdk 'Java8' 
    }
    stages {
        stage ('afw-core') {
            steps {
                withMaven(
                     // Maven installation declared in the Jenkins "Global Tool Configuration"
                     maven: 'Maven3',

                    // Use `$WORKSPACE/.repository` for local repository folder to avoid shared repositories
                    mavenLocalRepo: '.repository',
                ) {
				    script {
				        if (isUnix()) {
    			            sh 'mvn -fafw/afw-core/pom.xml -Pjacoco -Dmaven.test.failure.ignore=true clean install'
					    } else {
    			            bat 'mvn.cmd -fafw/afw-core/pom.xml -Pjacoco -Dmaven.test.failure.ignore=true clean install'
					    }
					}
                }
            }
        }
        stage ('afw-bootstrap') {
            steps {
                withMaven(
                     // Maven installation declared in the Jenkins "Global Tool Configuration"
                     maven: 'Maven3',

                    // Use `$WORKSPACE/.repository` for local repository folder to avoid shared repositories
                    mavenLocalRepo: '.repository',
                ) {
				    script {
				        if (isUnix()) {
    			            sh 'mvn -fafw/afw-bootstrap/pom.xml -Pjacoco -Dmaven.test.failure.ignore=true clean install'
					    } else {
    			            bat 'mvn.cmd -fafw/afw-bootstrap/pom.xml -Pjacoco -Dmaven.test.failure.ignore=true clean install'
					    }
				    }
                }
            }
        }
    }
}
