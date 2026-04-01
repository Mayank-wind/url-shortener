const form = document.getElementById("shortenForm");
const resultBox = document.getElementById("result");
const errorBox = document.getElementById("error");
const loadUrlsBtn = document.getElementById("loadUrlsBtn");
const urlList = document.getElementById("urlList");

async function loadUrls() {
    urlList.innerHTML = "Loading...";

    try {
        const response = await fetch("/api/urls");
        const data = await response.json();

        if (!response.ok) {
            throw new Error("Failed to load URLs");
        }

        if (data.length === 0) {
            urlList.innerHTML = "<p>No URLs found yet.</p>";
            return;
        }

        urlList.innerHTML = data.map(item => {
            const shortLink = `${window.location.origin}/api/${item.shortUrl}`;
            return `
                <div class="url-item">
                    <strong>${item.shortUrl}</strong>
                    <div>Original: ${item.originalUrl}</div>
                    <div>Short: <a href="${shortLink}" target="_blank">${shortLink}</a></div>
                    <small>Clicks: ${item.clickCount}</small>
                    <small>Created: ${item.createdAt || "N/A"}</small>
                    <small>Expires: ${item.expiresAt || "Never"}</small>
                    <button class="delete-btn" onclick="deleteUrl('${item.shortUrl}')">Delete</button>
                </div>
            `;
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

