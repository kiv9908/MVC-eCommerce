// Main JavaScript file for the application

document.addEventListener('DOMContentLoaded', function() {
    console.log('Document loaded and ready');
    
    // Add event listeners and other initialization here
    initializeFormValidation();
});

// Form validation
function initializeFormValidation() {
    const forms = document.querySelectorAll('form[data-validate="true"]');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            let isValid = true;
            
            // Find all required fields
            const requiredFields = form.querySelectorAll('[required]');
            
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    // Add error class or show error message
                    showError(field, 'This field is required');
                } else {
                    // Remove error class or hide error message
                    hideError(field);
                }
            });
            
            // Email validation
            const emailFields = form.querySelectorAll('input[type="email"]');
            emailFields.forEach(field => {
                if (field.value && !isValidEmail(field.value)) {
                    isValid = false;
                    showError(field, 'Please enter a valid email address');
                }
            });
            
            // Prevent form submission if validation fails
            if (!isValid) {
                event.preventDefault();
            }
        });
    });
}

function showError(field, message) {
    // Get or create error element
    let errorElement = field.nextElementSibling;
    if (!errorElement || !errorElement.classList.contains('error-message')) {
        errorElement = document.createElement('div');
        errorElement.className = 'error-message';
        field.parentNode.insertBefore(errorElement, field.nextSibling);
    }
    
    // Set error message and add error class to field
    errorElement.textContent = message;
    field.classList.add('error-field');
}

function hideError(field) {
    // Remove error message and class
    const errorElement = field.nextElementSibling;
    if (errorElement && errorElement.classList.contains('error-message')) {
        errorElement.textContent = '';
    }
    field.classList.remove('error-field');
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Add other utility functions as needed
