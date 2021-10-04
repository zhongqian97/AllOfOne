/**
 * 
 */
package ink.mastermind.AllINOne.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.RegionConflictException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;

/**
 * @author joshua
 *
 */
public class MinioClientUtils {
	 
    private static final Logger LOGGER = LoggerFactory.getLogger(MinioClientUtils.class);
    
    private static String url = "http://127.0.0.1:9000";
    private static String username = "minioadmin";
    private static String password = "minioadmin";    
    private static MinioClient minioClient;
    private static MinioClientUtils minioClientUtils;

    public static MinioClientUtils getInstance() {
        if (minioClientUtils != null && minioClient != null) {
            return minioClientUtils;
        }
        synchronized (MinioClientUtils.class) {
        	 minioClientUtils = new MinioClientUtils();
        }
        return minioClientUtils;
    }
 
 
    private MinioClientUtils() {
        init();
    }
 
    private void init() {
        try {
            if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                minioClient = new MinioClient(url, username, password, false);
            }
        } catch (Exception e) {
            LOGGER.error("restClient.close occur error", e);
        }
 
    }
	/**
	 * @param bucketName
	 * @param objectName
	 * @param stream
	 * 上传文件
	 */
	public static void uploadFile(String bucketName, String objectName, InputStream stream, PutObjectOptions putObjectOptions) {
		// TODO Auto-generated method stub
		try {
			if (minioClient.bucketExists(bucketName) == false) {
				minioClient.makeBucket(bucketName, null);
			}
			minioClient.putObject(bucketName, objectName, stream, putObjectOptions);
		} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | XmlParserException
				| ErrorResponseException | InternalException | IllegalArgumentException | InsufficientDataException
				| InvalidResponseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RegionConflictException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	public static byte[] downloadFile(String bucketName, String objectName) {
		// TODO Auto-generated method stub
		try {
			return toByteArray(minioClient.getObject(bucketName, objectName));
		} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
				| XmlParserException | ErrorResponseException | InternalException | IllegalArgumentException
				| InvalidResponseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	public static boolean exist(String bucketName, String objectName) {
		try {
			return toByteArray(minioClient.getObject(bucketName, objectName)).length > 0;
		} catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException | InsufficientDataException
				| XmlParserException | ErrorResponseException | InternalException | IllegalArgumentException
				| InvalidResponseException | IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param bucketName
	 * @param objectName
	 */
	public static void deleteFile(String bucketName, String objectName) {
		try {
			if (minioClient.bucketExists(bucketName) == false) {
				return;
			}
			minioClient.removeObject(bucketName, objectName);
		} catch (InvalidKeyException | InvalidBucketNameException | IllegalArgumentException | NoSuchAlgorithmException
				| InsufficientDataException | XmlParserException | ErrorResponseException | InternalException
				| InvalidResponseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static byte[] toByteArray(InputStream input) throws IOException {
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    byte[] buffer = new byte[4096];
	    int n = 0;
	    while (-1 != (n = input.read(buffer))) {
	        output.write(buffer, 0, n);
	    }
	    return output.toByteArray();
	}
	
	public static void main(String[] args) {
		try {
			InputStream stream = new FileInputStream("/Users/joshua/get-docker.sh");
			PutObjectOptions putObjectOptions = new PutObjectOptions(13216, -1);
			MinioClientUtils.getInstance().uploadFile("task", "1234", stream, putObjectOptions);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * @param bucketName
	 * @return
	 */
	public LinkedList<String> findAll(String bucketName) {
		// TODO Auto-generated method stub
		try {
			Iterable<Result<Item>> list = minioClient.listObjects(bucketName);
			LinkedList<String> files = new LinkedList<String>();
			Iterator<Result<Item>> it = list.iterator();
			while (it.hasNext()) {
				Result<Item> i = it.next();
				try {
					files.add(i.get().objectName());
				} catch (InvalidKeyException | InvalidBucketNameException | IllegalArgumentException
						| NoSuchAlgorithmException | InsufficientDataException | ErrorResponseException
						| InternalException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return files;
		} catch (XmlParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
 