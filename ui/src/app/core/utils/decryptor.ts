import * as CryptoJS from 'crypto-js';
import { environment } from '@environment/environment';

export const decrypt = (encryptedString: string): string => {
    const iterationCount = 65536;
    const keySize = 256;
    const encryptionKey = environment.encryptionKey;
    const iv = environment.iv;
    const salt = environment.salt;


    const AesUtil = function (keySize: number, iterationCount: number) {
        this.keySize = keySize / 32;
        this.iterationCount = iterationCount;
    };

    AesUtil.prototype.generateKey = function (salt, passPhrase) {
        const key = CryptoJS.PBKDF2(passPhrase, CryptoJS.enc.Hex.parse(salt),
            { keySize: this.keySize, iterations: this.iterationCount });
        return key;
    };

    AesUtil.prototype.decrypt = function (salt, iv, passPhrase, cipherText) {
        const key = this.generateKey(salt, passPhrase);
        const cipherParams = CryptoJS.lib.CipherParams.create({
            ciphertext: CryptoJS.enc.Base64.parse(cipherText)
        });
        const decrypted = CryptoJS.AES.decrypt(cipherParams, key,
            { iv: CryptoJS.enc.Hex.parse(iv) });
        return decrypted.toString(CryptoJS.enc.Utf8);
    };

    const aesUtil = new AesUtil(keySize, iterationCount);
    const plaintext = aesUtil.decrypt(salt, iv, encryptionKey, encryptedString);
    return plaintext;
};
