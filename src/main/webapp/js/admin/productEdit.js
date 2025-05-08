/**
 * 상품 수정 페이지의 이미지 업로드 관련 JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    // 이미지 선택 시 미리보기
    const productImageInput = document.getElementById('productImage');
    const newImagePreview = document.getElementById('newImagePreview');
    const newImagePreviewImg = document.getElementById('newImagePreviewImg');
    const fileNameDisplay = document.getElementById('fileNameDisplay');
    
    // 기존 파일 정보 가져오기
    const fileId = document.querySelector('input[name="fileId"]')?.value;
    if (fileId && fileId.trim() !== "") {
        // AJAX로 파일 정보 가져오기
        fetch(contextPath + '/admin/product/file-info?fileId=' + fileId)
            .then(response => response.json())
            .then(data => {
                if (data && data.originalFileName) {
                    // 파일 이름 표시
                    if (fileNameDisplay) {
                        fileNameDisplay.textContent = data.originalFileName;
                    }
                }
            })
            .catch(error => {
                console.error('파일 정보 가져오기 오류:', error);
                // 오류 시 기본 텍스트 표시
                if (fileNameDisplay) {
                    fileNameDisplay.textContent = "파일명 없음";
                }
            });
    }
    
    if (productImageInput) {
        productImageInput.addEventListener('change', function(e) {
            console.log('파일 선택 변경 감지됨');
            
            if (this.files && this.files[0]) {
                console.log('파일 선택됨:', this.files[0].name);
                const reader = new FileReader();
                const fileName = this.files[0].name;
                
                // 파일명 표시 업데이트 - 새 파일명만 간단히 표시
                if (fileNameDisplay) {
                    fileNameDisplay.textContent = fileName;
                }
                
                reader.onload = function(e) {
                    console.log('파일 읽기 완료');
                    newImagePreviewImg.src = e.target.result;
                    newImagePreview.style.display = 'block';
                    
                    // 이미지 파일을 선택하면 삭제 체크박스 비활성화
                    const deleteCheckbox = document.getElementById('fileDeleteOption');
                    if (deleteCheckbox) {
                        deleteCheckbox.checked = false;
                        deleteCheckbox.disabled = true;
                    }
                };
                
                reader.onerror = function(e) {
                    console.error('파일 읽기 오류:', e);
                };
                
                reader.readAsDataURL(this.files[0]);
            } else {
                console.log('파일이 선택되지 않음');
                newImagePreview.style.display = 'none';
                
                // 파일 선택 취소 시 원래 파일명으로 되돌리기
                if (fileId && fileId.trim() !== "" && fileNameDisplay) {
                    // 원래 파일 정보 다시 가져오기
                    fetch(contextPath + '/admin/product/file-info?fileId=' + fileId)
                        .then(response => response.json())
                        .then(data => {
                            if (data && data.originalFileName) {
                                fileNameDisplay.textContent = data.originalFileName;
                            }
                        })
                        .catch(error => {
                            console.error('파일 정보 가져오기 오류:', error);
                            fileNameDisplay.textContent = "파일명 없음";
                        });
                }
            }
        });
    } else {
        console.error('productImage 요소를 찾을 수 없습니다.');
    }
    
    // 삭제 체크박스 이벤트
    const deleteCheckbox = document.getElementById('fileDeleteOption');
    if (deleteCheckbox) {
        deleteCheckbox.addEventListener('change', function(e) {
            const fileInput = document.getElementById('productImage');
            if (this.checked) {
                fileInput.disabled = true;
                newImagePreview.style.display = 'none';
                
                // 삭제 선택 시 표시 업데이트
                if (fileNameDisplay) {
                    fileNameDisplay.textContent = "삭제 예정";
                    fileNameDisplay.style.color = "#dc3545";
                }
            } else {
                fileInput.disabled = false;
                
                // 삭제 취소 시 원래 파일명으로 되돌리기
                if (fileId && fileId.trim() !== "" && fileNameDisplay) {
                    fileNameDisplay.style.color = "";
                    // 원래 파일 정보 다시 가져오기
                    fetch(contextPath + '/admin/product/file-info?fileId=' + fileId)
                        .then(response => response.json())
                        .then(data => {
                            if (data && data.originalFileName) {
                                fileNameDisplay.textContent = data.originalFileName;
                            }
                        })
                        .catch(error => {
                            console.error('파일 정보 가져오기 오류:', error);
                            fileNameDisplay.textContent = "파일명 없음";
                        });
                }
            }
        });
    }
});
