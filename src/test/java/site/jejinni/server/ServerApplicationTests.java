package site.jejinni.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void jasyptTest() {
		String plainText = "my_secret_password"; // 암호화할 실제 비밀번호
		String key = "my_master_key"; // 환경변수로 설정할 Master Key

		org.jasypt.encryption.pbe.StandardPBEStringEncryptor encryptor = new org.jasypt.encryption.pbe.StandardPBEStringEncryptor();
		encryptor.setAlgorithm("PBEWithMD5AndDES");
		encryptor.setPassword(key);

		String encryptedText = encryptor.encrypt(plainText);
		String decryptedText = encryptor.decrypt(encryptedText);

		System.out.println("Plain Text: " + plainText);
		System.out.println("Encrypted: " + encryptedText);
		System.out.println("Decrypted: " + decryptedText);
		
		// 결과가 일치하는지 확인
		assert(plainText.equals(decryptedText));
	}
}
