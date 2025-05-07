// userList.js
document.addEventListener("DOMContentLoaded", function() {
  // 검색 기능
  document.getElementById("searchButton").addEventListener("click", function() {
    const searchValue = document.getElementById("searchInput").value.toLowerCase();
    filterTable(searchValue);
  });

  document.getElementById("searchInput").addEventListener("keyup", function(event) {
    if (event.key === "Enter") {
      const searchValue = document.getElementById("searchInput").value.toLowerCase();
      filterTable(searchValue);
    }
  });

  // 상태별 필터링
  document.getElementById("filterAll").addEventListener("click", function() {
    showAllRows();
  });

  document.getElementById("filterActive").addEventListener("click", function() {
    filterByStatus("ST01");
  });

  document.getElementById("filterRequest").addEventListener("click", function() {
    filterByStatus("ST00");
  });

  document.getElementById("filterInactive").addEventListener("click", function() {
    filterByStatus("ST02");
  });

  document.getElementById("filterStop").addEventListener("click", function() {
    filterByStatus("ST03");
  });

  // 테이블 행 필터링 함수
  function filterTable(searchValue) {
    const rows = document.querySelectorAll("#userTable tbody tr");

    rows.forEach(row => {
      const name = row.cells[1].textContent.toLowerCase();
      const email = row.cells[2].textContent.toLowerCase();

      if (name.includes(searchValue) || email.includes(searchValue)) {
        row.style.display = "";
      } else {
        row.style.display = "none";
      }
    });
  }

  // 상태별 필터링 함수
  function filterByStatus(status) {
    const rows = document.querySelectorAll("#userTable tbody tr");

    rows.forEach(row => {
      if (row.getAttribute("data-status") === status) {
        row.style.display = "";
      } else {
        row.style.display = "none";
      }
    });
  }

  // 모든 행 표시 함수
  function showAllRows() {
    const rows = document.querySelectorAll("#userTable tbody tr");
    rows.forEach(row => {
      row.style.display = "";
    });
  }
});