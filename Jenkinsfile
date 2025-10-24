pipeline {
    agent any

    environment {
        SPRING_DATASOURCE_URL      = "jdbc:mysql://database-park-control.c2pkw8qy64pz.us-east-1.rds.amazonaws.com:3306/park_control_db"
        SPRING_DATASOURCE_USERNAME = credentials('db-user')
        SPRING_DATASOURCE_PASSWORD = credentials('db-password')
        SERVER_PORT                = "8081"

        AWS_REGION                 = "us-east-1"
        AWS_ACCESS_KEY_ID          = credentials('aws-key')
        AWS_SECRET_ACCESS_KEY      = credentials('aws-secret')
        S3_BUCKET_FRONTEND         = "delivery-system-frontend"
        S3_BUCKET_BACKEND          = "mi-proyecto-backend-storage"

        // ------------------------
        // Backend y Frontend
        // ------------------------
        CORS_ALLOWED_ORIGINS  = "http://delivery-system-frontend.s3-website.us-east-2.amazonaws.com"

        STORAGE_TYPE               = "s3"
        RUTA_LOCAL                 = "/tmp/uploads/"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // stage('Compile & Test') {
        //     steps {
        //         sh 'cd mvn test'
        //     }
        // }


        stage('Deploy') {
             steps {
                sh 'cd ApiParkControl && mvn package -DskipTests'
                sh 'cd ApiParkControl/target/config-dev-docker && sudo -E docker-compose down'
                sh 'cd ApiParkControl/target/config-dev-docker && sudo -E docker-compose up --build -d'
                echo "Despliegue a DEV exitoso"
            }
        }
    }
}
