mvn clean install -Dspring-boot.run.profiles=production -DENCRYPTION_SECRET=something_only_joy_knows

mvn spring-boot:run -D"spring-boot.run.profiles"=production -Dspring-boot.run.jvmArguments="-DENCRYPTION_SECRET=something_only_joy_knows"

