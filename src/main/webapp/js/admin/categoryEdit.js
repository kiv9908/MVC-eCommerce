/**
 * 카테고리 편집 페이지 JavaScript
 */
$(document).ready(function() {
    // 상위 카테고리 선택 드롭다운에 레벨별 들여쓰기 적용
    $('#parentId option').each(function() {
        var level = $(this).data('level');
        if (level) {
            $(this).addClass('indent-level-' + level);
        }
    });

    // 상위 카테고리와 카테고리명 변경 시, 전체 카테고리명 미리보기 표시
    $('#parentId, #name').on('change input', function() {
        updateFullCategoryName();
    });

    // 전체 카테고리명 자동 업데이트 함수
    function updateFullCategoryName() {
        var name = $('#name').val().trim();
        var parentId = $('#parentId').val();

        if (name) {
            if (parentId) {
                var parentFullName = $('#parentId option:selected').data('fullname');
                if (!parentFullName) {
                    parentFullName = $('#parentId option:selected').text().trim();
                }
                $('#fullName').val(parentFullName + ' > ' + name);
            } else {
                // 상위 카테고리가 없는 경우 (대분류)
                $('#fullName').val(name);
            }
        } else {
            $('#fullName').val('');
        }
    }

    // 페이지 로드 시 초기화
    updateFullCategoryName();

    // 삭제 버튼 클릭 시 확인 대화상자 표시
    $('.btn-danger[title="매핑 삭제"]').on('click', function(e) {
        if (!confirm('이 카테고리를 삭제하시겠습니까? 하위 카테고리가 있는 경우 함께 삭제됩니다.')) {
            e.preventDefault();
        }
    });
});