/**
 * Initializes the document and sets up the form submission handler.
 * Uses modern JavaScript features with vanilla DOM manipulation.
 */
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('shortener');
    const urlInput = document.getElementById('urlInput');

    /**
     * Handles the form submission event.
     * Prevents the default form submission and sends an AJAX POST request.
     * @param {Event} event - The form submission event.
     */
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        
        const url = urlInput.value.trim();
        
        if (!isValidURL(url)) {
            showError('Please enter a valid URL');
            return;
        }
        
        setLoading(true);
        clearResult();
        
        // Use modern FormData with multipart/form-data
        const formData = new FormData();
        formData.append('url', url);
        
        fetch('/api/link', {
            method: 'POST',
            body: formData,
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                return response.json().then(data => {
                    const shortURL = response.headers.get('Location') || data.url;
                    showSuccess(shortURL);
                });
            } else {
                return response.json().then(errorData => {
                    // Handle Problem Details format (RFC 9457)
                    const errorMessage = errorData.detail || errorData.title || 'Failed to shorten URL';
                    showError(errorMessage);
                }).catch(() => {
                    showError('Failed to shorten URL');
                });
            }
        })
        .catch(error => {
            console.error('Error shortening URL:', error);
            showError('Network error. Please try again.');
        })
        .finally(() => {
            setLoading(false);
        });
    });
    
    // Add input validation
    urlInput.addEventListener('input', function() {
        const url = this.value.trim();
        const isValid = !url || isValidURL(url);
        
        if (url && isValid) {
            this.classList.remove('is-invalid');
            this.classList.add('is-valid');
        } else if (url && !isValid) {
            this.classList.remove('is-valid');
            this.classList.add('is-invalid');
        } else {
            this.classList.remove('is-valid', 'is-invalid');
        }
    });
});

/**
 * Check if URL is valid
 * @param {string} url - The URL to validate
 * @returns {boolean} True if valid
 */
function isValidURL(url) {
    try {
        new URL(url);
        return true;
    } catch {
        return false;
    }
}

/**
 * Set loading state
 * @param {boolean} isLoading - Whether to show loading
 */
function setLoading(isLoading) {
    const submitBtn = document.getElementById('submitBtn');
    if (isLoading) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Shortening...';
    } else {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Short me!';
    }
}

/**
 * Show success message with shortened URL
 * @param {string} shortURL - The shortened URL
 */
function showSuccess(shortURL) {
    document.getElementById('result').innerHTML = `
        <div class='alert alert-success lead'>
            <strong>Success!</strong> Your URL has been shortened:
            <br><br>
            <a target='_blank' href='${shortURL}' style="color: white; text-decoration: underline; font-weight: 600;">
                ${shortURL}
            </a>
            <br><br>
            <button class="btn btn-sm btn-outline-secondary" onclick="copyToClipboard('${shortURL}')">
                Copy URL
            </button>
        </div>
    `;
    const urlInput = document.getElementById('urlInput');
    urlInput.value = '';
    urlInput.classList.remove('is-valid', 'is-invalid');
    
    // Focus back to input for next URL
    setTimeout(() => urlInput.focus(), 100);
}

/**
 * Show error message
 * @param {string} message - The error message
 */
function showError(message) {
    document.getElementById('result').innerHTML = `
        <div class='alert alert-danger lead'>
            <strong>Error:</strong> ${message}
        </div>
    `;
}

/**
 * Clear result area
 */
function clearResult() {
    document.getElementById('result').innerHTML = '';
}

/**
 * Copy text to clipboard
 * @param {string} text - The text to copy
 */
function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            // Show feedback
            const btn = event.target;
            const originalText = btn.textContent;
            btn.textContent = 'Copied!';
            setTimeout(() => {
                btn.textContent = originalText;
            }, 2000);
        }).catch(() => {
            // Fallback for older browsers
            fallbackCopyToClipboard(text);
        });
    } else {
        fallbackCopyToClipboard(text);
    }
}

/**
 * Fallback copy to clipboard for older browsers
 * @param {string} text - The text to copy
 */
function fallbackCopyToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    try {
        document.execCommand('copy');
        const btn = event.target;
        const originalText = btn.textContent;
        btn.textContent = 'Copied!';
        setTimeout(() => {
            btn.textContent = originalText;
        }, 2000);
    } catch (err) {
        console.error('Failed to copy: ', err);
    }
    document.body.removeChild(textArea);
}