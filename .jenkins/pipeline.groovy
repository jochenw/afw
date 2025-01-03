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
    			            sh 'mvn -V -fpom.xml -Pjacoco clean install javadoc:javadoc'
					    } else {
    			            bat 'mvn.cmd -V -fpom.xml -Pjacoco clean install javadoc:javadoc'
					    }
					}
                }
            }
        }
	stage ('Collect Profiler Data') {
	    steps {
                // step( [ $class: 'JacocoPublisher' ] )
		jacoco(execPattern: '**/*.exec',
		       classPattern: '**/target/classes/**',
		       exclusionPattern: '**/com/github/jochenw/afw/core/el/jcc/*.class')
	    }
	}
	stage ('collect profiler data') {
            steps {
	        recordCoverage(tools: [[parser: 'JACOCO']],
		               sourceCodeRetention: 'MODIFIED',
		               sourceDirectories: [[glob: '**/src/main/java']])
	    }
	}
    }
}
