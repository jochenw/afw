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
                ) {
				    script {
				        def props = readproperties defaults: defaultprops, interpolate=true, file=".jenkins/build.properties"
				        if (isUnix()) {
    			            sh 'mvn -fpom.xml -Pjacoco -Dmaven.test.failure.ignore=true clean install'
					    } else {
    			            bat 'mvn.cmd -fpom.xml -Pjacoco -Dmaven.test.failure.ignore=true clean install'
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
