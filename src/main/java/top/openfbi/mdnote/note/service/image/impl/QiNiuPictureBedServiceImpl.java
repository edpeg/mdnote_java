package top.openfbi.mdnote.note.service.image.impl;

import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.openfbi.mdnote.common.ResultStatus;
import top.openfbi.mdnote.common.exception.ResultException;
import top.openfbi.mdnote.note.model.Image;
import top.openfbi.mdnote.note.service.image.PictureBedInterface;
import top.openfbi.mdnote.utils.Hash;

import java.io.IOException;

/**
 * 文件上传类
 */
@Component
@Service
public class QiNiuPictureBedServiceImpl implements PictureBedInterface {
    // 上传秘钥
    private String accessKey;
    private String secretKey;
    // 设置上传空间名
    private String bucket;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    static String URL_PREFIX = "http://taukc4pwe.hn-bkt.clouddn.com/";

    @Value(value = "${qiniu-store.access-key}")
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    @Value(value = "${qiniu-store.secret-key}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Value(value = "${qiniu-store.bucket}")
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * 上传图片至七牛云
     */
    public Image save(MultipartFile uploadFile) throws ResultException {
        String originalFileName = uploadFile.getOriginalFilename();
        // 保存文件后缀类型
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        byte[] fileBytes;
        String fileMd5;
        // 使用md5计算文件哈希值作为图片id
        try {
            fileBytes = uploadFile.getBytes();
            fileMd5 = Hash.md5(fileBytes);
        } catch (IOException e) {
            logger.warn("文件读取错误，上传失败");
            throw new ResultException(ResultStatus.FILE_READ_FALL);
        }

        // 设置上传大区地址(北美)
        Configuration cfg = new Configuration(Region.huanan());
        // 创建上传类
        UploadManager uploadManager = new UploadManager(cfg);
        // 设置上传秘钥
        Auth auth = Auth.create(accessKey, secretKey);
        // 生成上传token
        String upToken = auth.uploadToken(bucket);

        DefaultPutRet putRet;
        try {
            // 开始上传图片
            Response response = uploadManager.put(fileBytes, fileMd5 + extension, upToken);
            // 上传完回复对象
            putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
        } catch (QiniuException qiniuException) {
            logger.error("七牛云图片上传失败");
            logger.error(qiniuException.getMessage());
            throw new ResultException(ResultStatus.QI_NIU_FILE_UPLOAD_FALL);
        }

        Image image = new Image();
        // 获取文件名
        image.setName(originalFileName);
        // 设置文件URL和文件在七牛云的文件名
        image.setFileUrl(URL_PREFIX + putRet.key);
        // 返回文件信息
        return image;
    }

}
