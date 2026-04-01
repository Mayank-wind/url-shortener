const form = document.getElementById("shortenForm");
const resultBox = document.getElementById("result");
const errorBox = document.getElementById("error");

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
    } catch (error) {
        errorBox.textContent = error.message;
        errorBox.classList.remove("hidden");
    }
});
