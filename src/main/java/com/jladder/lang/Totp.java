package com.jladder.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Totp计算类
 */
public class Totp {
    private static final Logger LOGGER = LoggerFactory.getLogger(Totp.class);
    private Totp() {}
    private static final long Duration=30;
    /**
     * This method uses the JCE to provide the crypto algorithm.
     * HMAC computes a Hashed Message Authentication Code with the
     * crypto hash algorithm as a parameter.
     * @param crypto: the crypto algorithm (HmacSHA1, HmacSHA256,HmacSHA512)
     * @param keyBytes: the bytes to use for the HMAC key
     * @param text: the message or text to be authenticated
     */
    private static byte[] hmac_sha(String crypto, byte[] keyBytes,byte[] text){
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    /**
     * 十六进制文本转字节集
     * @param hex 十六进制文本
     * @return: 字节集
     */
    private static byte[] hexStr2Bytes(String hex){
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex,16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++)
            ret[i] = bArray[i+1];
        return ret;
    }
    // 0 1  2   3    4     5      6       7        8
    private static final int[] DIGITS_POWER= {1,10,100,1000,10000,100000,1000000,10000000,100000000 };

    /**
     *生成TOTP码
     * @param key Base32解码后的Key
     * @param time 时间步进值
     * @param digits 整数位
     * @return
     */
    public static String create(String key, String time,  String digits){
        return create(key, time, digits, "HmacSHA1");
    }

    /**
     *生成TOTP码
     * @param key Base32解码后的Key
     * @param time 时间步进值
     * @param digits 整数位
     * @return
     */
    public static String createBy256(String key,String time,String digits){
        return create(key, time, digits, "HmacSHA256");
    }

    /**
     *生成TOTP码
     * @param key Base32解码后的Key
     * @param time 时间步进值
     * @param digits 整数位
     * @return
     */
    public static String createBy512(String key,String time,String digits){
        return create(key, time, digits, "HmacSHA512");
    }

    /**
     * 生成TOTP码
     * @param key Base32解码后的Key
     * @param time 时间步进值
     * @param digits 整数位
     * @param crypto 加密算法 HmacSHA1,HmacSHA512,HmacSHA256,HmacSHA1
     * @return
     */
    public static String create(String key, String time, String digits,String crypto){
        int codeDigits = Integer.decode(digits).intValue();
        String result = null;
        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        while (time.length() < 16 )
            time = "0" + time;
        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);
        byte[] hash = hmac_sha(crypto, k, msg);
        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;
        int binary =((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);
        int otp = binary % DIGITS_POWER[codeDigits];
        result = Integer.toString(otp);
        while (result.length() < codeDigits) {
            result = "0" + result;
        }
        return result;
    }

    /**
     * 验证动态口令是否正确
     * @param secretBase32 密钥
     * @param code 待验证的动态口令
     * @return
     */
    public static boolean verify(String secretBase32, String code){
        return create(secretBase32).equals(code);
    }

    /**
     * 生成totp协议字符串
     * @param user 用户
     * @param domain 域名称
     * @param base32 base32编码格式的密钥
     * @return
     */
    public static String createUrl(String user, String domain, String base32){
        return "otpauth://totp/" + domain + ":" + user + "?secret=" + base32;
    }
    /**
     * 根据密钥生成动态口令
     * @param base32 base32编码格式的密钥
     * @return
     */
    public static String create(String base32){
        return create(base32,Duration);
    }
    /**
     * 根据密钥生成动态口令
     * @param base32 base32编码格式的密钥
     * @param duration 失效时长，一般为30s
     * @return
     */
    public static String create(String base32,long duration){
        String secretHex = "";
        try {
            secretHex = HexEncoding.encode(Base32String.decode(base32));
        } catch (Base32String.DecodingException e) {
            LOGGER.error("解码" + base32 + "出错，", e);
            throw new RuntimeException("解码Base32出错");
        }
        String steps = "0";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        long currentTime = System.currentTimeMillis() / 1000L;
        try {
            long t = currentTime / duration;
            steps = Long.toHexString(t).toUpperCase();
            while (steps.length() < 16) steps = "0" + steps;
            return create(secretHex, steps, "6","HmacSHA1");
        } catch (final Exception e) {
            LOGGER.error("生成动态口令出错：" + base32, e);
            throw new RuntimeException("生成动态口令出错");
        }
    }
    /**
     * 生成base32编码的随机密钥
     * @param length
     * @return
     */
    public static String createSecret(int length){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[length / 2];
        random.nextBytes(salt);
        return Base32String.encode(salt);
    }
    /**
     * 生成base32编码的随机密钥
     * @param secret 加密源文本
     * @return
     */
    public static String createSecret(String secret){

        return Base32String.encode(secret.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 生成base32编码的随机密钥
     * @param user 用户
     * @param password 密码
     * @return
     */
    public static String createSecret(String user,String password){

        return Base32String.encode((user+password).getBytes(StandardCharsets.UTF_8));
    }
}

/**
 */
class HexEncoding {

    /** Hidden constructor to prevent instantiation. */
    private HexEncoding() {}

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Encodes the provided data as a hexadecimal string.
     */
    public static String encode(byte[] data) {
        StringBuilder result = new StringBuilder(data.length * 2);
        for (byte b : data) {
            result.append(HEX_DIGITS[(b >>> 4) & 0x0f]);
            result.append(HEX_DIGITS[b & 0x0f]);
        }
        return result.toString();
    }

    /**
     * Decodes the provided hexadecimal string into an array of bytes.
     */
    public static byte[] decode(String encoded) {
        // IMPLEMENTATION NOTE: Special care is taken to permit odd number of hexadecimal digits.
        int resultLengthBytes = (encoded.length() + 1) / 2;
        byte[] result = new byte[resultLengthBytes];
        int resultOffset = 0;
        int encodedCharOffset = 0;
        if ((encoded.length() % 2) != 0) {
            // Odd number of digits -- the first digit is the lower 4 bits of the first result byte.
            result[resultOffset++] = (byte) getHexadecimalDigitValue(encoded.charAt(encodedCharOffset));
            encodedCharOffset++;
        }
        for (int len = encoded.length(); encodedCharOffset < len; encodedCharOffset += 2) {
            result[resultOffset++] = (byte)
                    ((getHexadecimalDigitValue(encoded.charAt(encodedCharOffset)) << 4)
                            | getHexadecimalDigitValue(encoded.charAt(encodedCharOffset + 1)));
        }
        return result;
    }

    private static int getHexadecimalDigitValue(char c) {
        if ((c >= 'a') && (c <= 'f')) {
            return (c - 'a') + 0x0a;
        } else if ((c >= 'A') && (c <= 'F')) {
            return (c - 'A') + 0x0a;
        } else if ((c >= '0') && (c <= '9')) {
            return c - '0';
        } else {
            throw new IllegalArgumentException("Invalid hexadecimal digit at position : '" + c + "' (0x" + Integer.toHexString(c) + ")");
        }
    }
}


class Base32String {
    // singleton

    private static final Base32String INSTANCE = new Base32String("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"); // RFC 4648/3548

    static Base32String getInstance() {
        return INSTANCE;
    }

    // 32 alpha-numeric characters.
    private String ALPHABET;
    private char[] DIGITS;
    private int MASK;
    private int SHIFT;
    private HashMap<Character, Integer> CHAR_MAP;

    static final String SEPARATOR = "-";

    protected Base32String(String alphabet) {
        this.ALPHABET = alphabet;
        DIGITS = ALPHABET.toCharArray();
        MASK = DIGITS.length - 1;
        SHIFT = Integer.numberOfTrailingZeros(DIGITS.length);
        CHAR_MAP = new HashMap<Character, Integer>();
        for (int i = 0; i < DIGITS.length; i++) {
            CHAR_MAP.put(DIGITS[i], i);
        }
    }

    public static byte[] decode(String encoded) throws DecodingException {
        return getInstance().decodeInternal(encoded);
    }

    protected byte[] decodeInternal(String encoded) throws DecodingException {
        // Remove whitespace and separators
        encoded = encoded.trim().replaceAll(SEPARATOR, "").replaceAll(" ", "");

        // Remove padding. Note: the padding is used as hint to determine how many
        // bits to decode from the last incomplete chunk (which is commented out
        // below, so this may have been wrong to start with).
        encoded = encoded.replaceFirst("[=]*$", "");

        // Canonicalize to all upper case
        encoded = encoded.toUpperCase(Locale.US);
        if (encoded.length() == 0) {
            return new byte[0];
        }
        int encodedLength = encoded.length();
        int outLength = encodedLength * SHIFT / 8;
        byte[] result = new byte[outLength];
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        for (char c : encoded.toCharArray()) {
            if (!CHAR_MAP.containsKey(c)) {
                throw new DecodingException("Illegal character: " + c);
            }
            buffer <<= SHIFT;
            buffer |= CHAR_MAP.get(c) & MASK;
            bitsLeft += SHIFT;
            if (bitsLeft >= 8) {
                result[next++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        // We'll ignore leftover bits for now.
        //
        // if (next != outLength || bitsLeft >= SHIFT) {
        //  throw new DecodingException("Bits left: " + bitsLeft);
        // }
        return result;
    }

    public static String encode(byte[] data) {
        return getInstance().encodeInternal(data);
    }

    protected String encodeInternal(byte[] data) {
        if (data.length == 0) {
            return "";
        }

        // SHIFT is the number of bits per output character, so the length of the
        // output is the length of the input multiplied by 8/SHIFT, rounded up.
        if (data.length >= (1 << 28)) {
            // The computation below will fail, so don't do it.
            throw new IllegalArgumentException();
        }

        int outputLength = (data.length * 8 + SHIFT - 1) / SHIFT;
        StringBuilder result = new StringBuilder(outputLength);

        int buffer = data[0];
        int next = 1;
        int bitsLeft = 8;
        while (bitsLeft > 0 || next < data.length) {
            if (bitsLeft < SHIFT) {
                if (next < data.length) {
                    buffer <<= 8;
                    buffer |= (data[next++] & 0xff);
                    bitsLeft += 8;
                } else {
                    int pad = SHIFT - bitsLeft;
                    buffer <<= pad;
                    bitsLeft += pad;
                }
            }
            int index = MASK & (buffer >> (bitsLeft - SHIFT));
            bitsLeft -= SHIFT;
            result.append(DIGITS[index]);
        }
        return result.toString();
    }

    @Override
    // enforce that this class is a singleton
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static class DecodingException extends Exception {
        public DecodingException(String message) {
            super(message);
        }
    }
}