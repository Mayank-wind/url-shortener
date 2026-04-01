const form = document.getElementById("shortenForm");
const resultBox = document.getElementById("result");
const errorBox = document.getElementById("error");
const loadUrlsBtn = document.getElementById("loadUrlsBtn");
const urlList = document.getElementById("urlList");
const searchInput = document.getElementById("searchInput");

async function loadUrls() {
    urlList.innerHTML = "Loading...";

    try {
        const response = await fetch("/api/urls");
        const data = await response.json();
        const searchTerm = searchInput.value.trim().toLowerCase();
        const filteredData = data.filter(item =>
            item.shortUrl.toLowerCase().includes(searchTerm) ||
            item.originalUrl.toLowerCase().includes(searchTerm)
        );

        if (!response.ok) {
            throw new Error("Failed to load URLs");
        }

        if (filteredData.length === 0) {
            urlList.innerHTML = "<p>No URLs found yet.</p>";
            return;
        }

        urlList.innerHTML = filteredData.map(item => {
            const shortLink = `${window.location.origin}/api/${item.shortUrl}`;
            return `
                <div class="url-item">
                    <strong>${item.shortUrl}</strong>
                    <div>Original: ${item.originalUrl}</div>
                    <div>
                        Short: <a href="${shortLink}" target="_blank">${shortLink}</a>
                        <button type="button" class="copy-btn" onclick="copyToClipboard('${shortLink}', this)">Copy</button>
                        <button type="button" class="qr-btn" onclick="toggleQr('qr-${item.shortUrl}', '${shortLink}')">Show QR</button>
                    </div>
                    <div id="qr-${item.shortUrl}" class="qr-container hidden"></div>
                    <small>Clicks: ${item.clickCount}</small>
                    <small>Created: ${item.createdAt || "N/A"}</small>
                    <small>Expires: ${item.expiresAt || "Never"}</small>
                    <small>Last Accessed: ${item.lastAccessedAt || "Never"}</small>
                    <div class="expiration-editor">
                        <input type="datetime-local" id="exp-${item.shortUrl}">
                        <button type="button" class="update-btn" onclick="updateExpiration('${item.shortUrl}')">Update Expiration</button>
                    </div>
                    <button class="delete-btn" onclick="deleteUrl('${item.shortUrl}')">Delete</button>
                </div> `;
        }).join("");
    } catch (error) {
        urlList.innerHTML = `<p>${error.message}</p>`;
    }
}

async function deleteUrl(shortUrl) {
    const confirmed = window.confirm(`Delete short URL "${shortUrl}"?`);
    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(`/api/${shortUrl}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            let message = "Failed to delete URL";
            try {
                const data = await response.json();
                message = data.message || message;
            } catch (e) {
                // ignore JSON parse issue
            }
            throw new Error(message);
        }

        loadUrls();
    } catch (error) {
        errorBox.textContent = error.message;
        errorBox.classList.remove("hidden");
    }
}
async function copyToClipboard(text, button) {
    try {
        await navigator.clipboard.writeText(text);
        const originalText = button.textContent;
        button.textContent = "Copied!";
        setTimeout(() => {
            button.textContent = originalText;
        }, 1500);
    } catch (error) {
        errorBox.textContent = "Failed to copy URL";
        errorBox.classList.remove("hidden");
    }
}
function toggleQr(containerId, shortLink) {
    const container = document.getElementById(containerId);

    if (!container.classList.contains("hidden")) {
        container.classList.add("hidden");
        container.innerHTML = "";
        return;
    }

    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=${encodeURIComponent(shortLink)}`;

    container.innerHTML = `
        <img src="${qrUrl}" alt="QR Code for short URL">
    `;
    container.classList.remove("hidden");
}

async function updateExpiration(shortUrl) {
    const input = document.getElementById(`exp-${shortUrl}`);
    const expiresAt = input.value;

    if (!expiresAt) {
        errorBox.textContent = "Please select an expiration date/time.";
        errorBox.classList.remove("hidden");
        return;
    }

    try {
        const response = await fetch(`/api/${shortUrl}/expiration`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ expiresAt })
        });

        if (!response.ok) {
            let message = "Failed to update expiration";
            try {
                const data = await response.json();
                message = data.message || message;
            } catch (e) {
                // ignore parse issue
            }
            throw new Error(message);
        }

        loadUrls();
    } catch (error) {
        errorBox.textContent = error.message;
        errorBox.classList.remove("hidden");
    }
}

loadUrlsBtn.addEventListener("click", loadUrls);
form.addEventListener("submit", async (event) => {
    event.preventDefault();

    resultBox.classList.add("hidden");
    errorBox.classList.add("hidden");

    const url = document.getElementById("url").value.trim();
    const customAlias = document.getElementById("customAlias").value.trim();
    const expiresAt = document.getElementById("expiresAt").value;

    const requestBody = {
        url: url
    };

    if (customAlias) {
        requestBody.customAlias = customAlias;
    }

    if (expiresAt) {
        requestBody.expiresAt = expiresAt;
    }

    try {
        const response = await fetch("/api/shorten", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Something went wrong");
        }

        const shortLink = `${window.location.origin}/api/${data.shortUrl}`;

        resultBox.innerHTML = `
            <strong>Short URL created successfully.</strong><br>
            Original URL: ${data.originalUrl}<br>
            Short URL: <a href="${shortLink}" target="_blank">${shortLink}</a>
            <button type="button" class="copy-btn" onclick="copyToClipboard('${shortLink}', this)">Copy</button>
            <button type="button" class="qr-btn" onclick="toggleQr('result-qr', '${shortLink}')">Show QR</button>
            <div id="result-qr" class="qr-container hidden"></div>
        `;

        resultBox.classList.remove("hidden");
        form.reset();
        loadUrls();
    } catch (error) {
        errorBox.textContent = error.message;
        errorBox.classList.remove("hidden");
    }
});
loadUrls();
searchInput.addEventListener("input", loadUrls);


