/**
 * 사용자 관리 페이지 스크립트
 */
$(document).ready(function() {
  // 검색 기능
  $("#searchButton").click(function() {
    const searchText = $("#searchInput").val().toLowerCase();
    $("#userTable tbody tr").filter(function() {
      $(this).toggle(
        $(this).text().toLowerCase().indexOf(searchText) > -1
      );
    });
  });
  
  // 엔터키로 검색
  $("#searchInput").keypress(function(e) {
    if (e.which == 13) {
      $("#searchButton").click();
    }
  });
  
  // 상태별 필터링
  $("#filterAll").click(function() {
    $("#userTable tbody tr").show();
  });

  $("#filterRequest").click(function() {
    $("#userTable tbody tr").hide();
    $("#userTable tbody tr[data-status='ST00']").show();
  });

  $("#filterActive").click(function() {
    $("#userTable tbody tr").hide();
    $("#userTable tbody tr[data-status='ST01']").show();
  });
  
  $("#filterInactive").click(function() {
    $("#userTable tbody tr").hide();
    $("#userTable tbody tr[data-status='ST02']").show();
  });
  
  $("#filterStop").click(function() {
    $("#userTable tbody tr").hide();
    $("#userTable tbody tr[data-status='ST03']").show();
  });
  
  // 회원 정보 수정 모달 표시
  $(".edit-btn").click(function() {
    const userId = $(this).data("user-id");
    const userName = $(this).data("user-name");
    const userEmail = $(this).data("user-email");
    const userMobile = $(this).data("user-mobile");
    const userType = $(this).data("user-type");
    const userStatus = $(this).data("user-status");
    
    $("#editUserId").val(userId);
    $("#editUserName").val(userName);
    $("#editUserEmail").val(userEmail);
    $("#editUserMobile").val(userMobile);
    $("#editUserType").val(userType);
    $("#editUserStatus").val(userStatus);
    
    const editUserModal = new bootstrap.Modal(document.getElementById('editUserModal'));
    editUserModal.show();
  });
  
  // 수정사항 저장
  $("#saveUserChanges").click(function() {
    const userId = $("#editUserId").val();
    const userName = $("#editUserName").val();
    const mobileNumber = $("#editUserMobile").val();
    const userType = $("#editUserType").val();
    const status = $("#editUserStatus").val();
    
    // 현재 로그인한 사용자 정보
    const currentUserId = sessionUserId;
    const isCurrentUserAdmin = isAdmin;
    const originalUserType = $(this).data("user-type") || $(".edit-btn[data-user-id='" + userId + "']").data("user-type");
    
    // 1. 기본 정보 수정
    $.ajax({
      url: contextPath + "/admin/modify",
      type: "POST",
      data: {
        userId: userId,
        action: "updateInfo",
        userName: userName,
        mobileNumber: mobileNumber
      },
      success: function(response) {
        if (response.success) {
          // 2. 상태 변경 요청
          $.ajax({
            url: contextPath + "/admin/modify",
            type: "POST",
            data: {
              userId: userId,
              action: "updateStatus",
              status: status
            },
            success: function(statusResponse) {
              // 3. 권한 변경 요청 (마지막에 실행)
              $.ajax({
                url: contextPath + "/admin/modify",
                type: "POST",
                data: {
                  userId: userId,
                  action: "updateRole",
                  userType: userType
                },
                success: function(roleResponse) {
                  // 현재 로그인한 사용자의 권한 변경 여부 확인
                  const isChangingToRegularUser = userType === "10"; // 일반 사용자로 변경하는 경우
                  
                  if(userId === currentUserId && isCurrentUserAdmin && isChangingToRegularUser) {
                    // 현재 로그인한 관리자의 권한이 일반 사용자로 변경된 경우
                    alert("관리자 권한이 변경되었습니다. 다시 로그인해주세요.");
                    window.location.href = contextPath + "/user/login?error=admin_only";
                  } else {
                    // 권한 변경이 현재 사용자에게 영향이 없는 경우
                    alert("회원 정보가 성공적으로 수정되었습니다.");
                    location.reload();
                  }
                },
                error: function() {
                  alert("권한 변경 중 오류가 발생했습니다.");
                }
              });
            },
            error: function() {
              alert("상태 변경 중 오류가 발생했습니다.");
            }
          });
        } else {
          alert("회원 정보 수정 중 오류가 발생했습니다: " + response.message);
        }
      },
      error: function() {
        alert("회원 정보 수정 요청 중 오류가 발생했습니다.");
      }
    });
    
    // 모달 닫기
    const editUserModal = bootstrap.Modal.getInstance(document.getElementById('editUserModal'));
    editUserModal.hide();
  });
});