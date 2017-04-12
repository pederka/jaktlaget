package net.ddns.peder.drevet.utils;

/**
 * Created by peder on 4/11/17.
 */

import android.content.Context;

import net.ddns.peder.drevet.MainActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;

import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
//import org.spongycastle.jcajce.provider.asymmetric.x509.KeyFactory;
import org.spongycastle.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;


public class CryptoUtil {
    private static final String PRIVATE_KEY_FILENAME = "MyPrivateKey";
    private static final String PUBLIC_KEY_FILENAME = "MyPublicKey";
    private static final int outputKeyLength = 256;

    private static KeyPair ReadKeyPair(Context context)
    {
        try
        {
            byte[] privateKeyBytes = ReadData(PRIVATE_KEY_FILENAME, context);
            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(privateKeyBytes);
            byte[] publicKeyBytes = ReadData(PRIVATE_KEY_FILENAME, context);
            PKCS8EncodedKeySpec pubspec =
                    new PKCS8EncodedKeySpec(privateKeyBytes);
            try {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = kf.generatePrivate(spec);
                kf = KeyFactory.getInstance("RSA");
                PublicKey publicKey = kf.generatePublic(spec);
                return new KeyPair(publicKey, privateKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static KeyPair GenerateKeyPair() throws NoSuchAlgorithmException {
        // EDIT - do not need to create SecureRandom, this is done automatically by init() if one is not provided
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(outputKeyLength);
        return keyPairGenerator.generateKeyPair();
    }

    public static KeyPair renewKeyPair(Context context) {
        try {
            KeyPair keyPair = GenerateKeyPair();
            WriteKeyPair(keyPair, context);
            return keyPair;
        }
        catch( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public static KeyPair CreateOrRetrieveKeyPair(Context context)
    {
        try
        {
            KeyPair keyPair = ReadKeyPair(context);
            if ( keyPair == null )
            {
                keyPair = GenerateKeyPair();
                WriteKeyPair(keyPair, context);
            }
            return keyPair;
        }
        catch( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    private static void WriteKeyPair(KeyPair key, Context context)
    {
        WriteData(key.getPrivate().getEncoded(), PRIVATE_KEY_FILENAME, context);
        WriteData(key.getPublic().getEncoded(), PUBLIC_KEY_FILENAME, context);
    }

    private static void WriteData(byte[] data, String filename, Context context)
    {
        FileOutputStream fOut = null;
        try {
            fOut = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fOut.write(data);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] ReadData(String filename, Context context) throws IOException
    {
        byte[] key = new byte[5096];
        Arrays.fill(key, (byte)0);
        FileInputStream fOut = null;
        try
        {
            fOut = context.openFileInput(filename);
            int length = fOut.read(key);
            byte[] key2 = new byte[length];
            System.arraycopy(key, 0, key2, 0, length);
            fOut.close();
            return key2;
        }
        catch(FileNotFoundException e)
        {
            return null;
        }
    }


    private static byte[] encrypt(byte[] data, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(byte[] data, PublicKey key) {
        try {
            Cipher cipher1 = Cipher.getInstance("RSA");
            cipher1.init(Cipher.DECRYPT_MODE, key);
            return cipher1.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
