package com.certifolio.server.global.service;

import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    private static final String BUCKET = "certifolio-test-bucket";
    private static final String REGION = "ap-northeast-2";

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucket", BUCKET);
        ReflectionTestUtils.setField(s3Service, "region", REGION);
    }

    @Test
    @DisplayName("정상 업로드 시 S3 putObject가 호출되고 올바른 URL을 반환한다")
    void uploadFile_success() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                "hello-s3".getBytes()
        );

        String url = s3Service.uploadFile(file, "profile");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest captured = requestCaptor.getValue();
        assertThat(captured.bucket()).isEqualTo(BUCKET);
        assertThat(captured.contentType()).isEqualTo("image/png");
        assertThat(captured.contentLength()).isEqualTo(file.getSize());
        assertThat(captured.key()).startsWith("profile/").endsWith(".png");

        assertThat(url).isEqualTo(
                "https://" + BUCKET + ".s3." + REGION + ".amazonaws.com/" + captured.key()
        );
    }

    @Test
    @DisplayName("dirName이 비어있으면 기본 디렉터리 'uploads'로 키가 생성된다")
    void uploadFile_blankDirName_usesDefaultDir() {
        MultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "data".getBytes()
        );

        s3Service.uploadFile(file, "  ");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        assertThat(requestCaptor.getValue().key()).startsWith("uploads/").endsWith(".pdf");
    }

    @Test
    @DisplayName("빈 파일 업로드 시 INVALID_INPUT 예외가 발생한다")
    void uploadFile_emptyFile_throwsInvalidInput() {
        MultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );

        assertThatThrownBy(() -> s3Service.uploadFile(emptyFile, "profile"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GeneralErrorCode.INVALID_INPUT);

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("null 파일 업로드 시 INVALID_INPUT 예외가 발생한다")
    void uploadFile_nullFile_throwsInvalidInput() {
        assertThatThrownBy(() -> s3Service.uploadFile(null, "profile"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GeneralErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("S3Exception 발생 시 INTERNAL_SERVER_ERROR로 변환된다")
    void uploadFile_s3Exception_throwsInternalServerError() {
        MultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", "x".getBytes()
        );
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage("denied").build())
                .build();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(s3Exception);

        assertThatThrownBy(() -> s3Service.uploadFile(file, "profile"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GeneralErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("정상 삭제 시 fileUrl에서 추출한 key로 deleteObject가 호출된다")
    void deleteFile_success() {
        String key = "profile/abc-123.png";
        String fileUrl = "https://" + BUCKET + ".s3." + REGION + ".amazonaws.com/" + key;

        s3Service.deleteFile(fileUrl);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(captor.getValue().key()).isEqualTo(key);
    }

    @Test
    @DisplayName("fileUrl이 null/빈 값이면 deleteObject를 호출하지 않는다")
    void deleteFile_blankUrl_doesNothing() {
        s3Service.deleteFile(null);
        s3Service.deleteFile("");
        s3Service.deleteFile("   ");

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("삭제 시 S3Exception 발생하면 INTERNAL_SERVER_ERROR로 변환된다")
    void deleteFile_s3Exception_throwsInternalServerError() {
        String fileUrl = "https://" + BUCKET + ".s3." + REGION + ".amazonaws.com/profile/x.png";
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage("denied").build())
                .build();
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(s3Exception);

        assertThatThrownBy(() -> s3Service.deleteFile(fileUrl))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(GeneralErrorCode.INTERNAL_SERVER_ERROR);
    }
}
