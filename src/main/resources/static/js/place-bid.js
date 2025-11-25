(() => {
    const productSummaryEl = document.getElementById("bid-product-summary");
    const productIdInput = document.getElementById("bid-product-id");
    const consumerNidInput = document.getElementById("bid-consumer-nid");
    const currentBidInput = document.getElementById("bid-current-amount");
    const bidAmountInput = document.getElementById("bid-amount");
    const bidDateInput = document.getElementById("bid-date");
    const bidStatusInput = document.getElementById("bid-status");
    const bidForm = document.getElementById("place-bid-form");
    const bidFeedback = document.getElementById("place-bid-feedback");
    const currentYearEl = document.getElementById("current-year");

    if (currentYearEl) {
        currentYearEl.textContent = new Date().getFullYear();
    }

    const parseProfile = () => {
        try {
            return JSON.parse(localStorage.getItem("heritagehubProfile") ?? localStorage.getItem("heritagehubSession"));
        } catch {
            return null;
        }
    };

    const showSummary = (content) => {
        if (!productSummaryEl) return;
        productSummaryEl.innerHTML = content;
    };

    const showFeedback = (message, type = "") => {
        if (!bidFeedback) return;
        bidFeedback.textContent = message;
        bidFeedback.className = `form-feedback ${type}`.trim();
    };

    const fetchJson = async (url, options = {}) => {
        const bases = [];
        const origin = new URL(window.location.href).origin;
        bases.push(origin);
        if (!origin.endsWith(":8080")) {
            bases.push("http://localhost:8080");
            bases.push("http://127.0.0.1:8080");
        }
        let lastError;
        const profile = parseProfile();
        const apiKey = profile?.apiKey;
        for (const base of bases) {
            try {
                const absolute = url.startsWith("http") ? url : `${base}${url}`;
                const headers = {
                    "Content-Type": "application/json",
                    ...(options.headers || {})
                };
                if (apiKey) {
                    headers["X-API-KEY"] = apiKey;
                }
                const response = await fetch(absolute, {
                    ...options,
                    headers
                });
                const text = await response.text();
                let data = null;
                if (text) {
                    try {
                        data = JSON.parse(text);
                    } catch {
                        data = null;
                    }
                }
                if (!response.ok) {
                    const message = data?.error || data?.message || response.statusText;
                    throw new Error(message);
                }
                return data;
            } catch (error) {
                lastError = error;
            }
        }
        throw lastError ?? new Error("Unable to reach server.");
    };

    const params = new URLSearchParams(window.location.search);
    const productIdParam = params.get("productId");
    if (!productIdParam) {
        showSummary("<p class=\"empty-state\">Missing product ID. Return to the <a href=\"index.html\">catalog</a>.</p>");
        bidForm?.classList.add("hidden");
        return;
    }

    const loadProduct = async () => {
        try {
            const product = await fetchJson(`/api/products/${productIdParam}`);
            productIdInput.value = product.id ?? productIdParam;
            const price = product.productPrice != null ? Number(product.productPrice).toFixed(2) : "N/A";
            showSummary(`
                <div class="product-card">
                    <figure>
                        <img src="${product.uploadImage || "images/hero.jpg"}" alt="${product.productName ?? "Product"}">
                    </figure>
                    <h3>${product.productName ?? "Product"}</h3>
                    <p class="meta">Category: ${product.category ?? "Unknown"} • Sale: ${product.saleType ?? "N/A"}</p>
                    <p class="price">Price: Tk ${price}</p>
                    <p>${product.description ?? ""}</p>
                    <p class="meta">Seller: ${product.sellerNid ?? "Unknown"}</p>
                </div>
            `);
        } catch (error) {
            showSummary(`<p class="empty-state">${error.message}</p>`);
            bidForm?.classList.add("hidden");
        }
    };

    const loadCurrentBid = async () => {
        try {
            const bids = await fetchJson(`/api/bids?productId=${productIdParam}`);
            if (!Array.isArray(bids) || bids.length === 0) {
                currentBidInput.value = "No bids yet";
                return;
            }
            const highest = bids
                .slice()
                .sort((a, b) => Number(b.bidAmount ?? 0) - Number(a.bidAmount ?? 0))[0];
            currentBidInput.value = `Tk ${Number(highest.bidAmount ?? 0).toFixed(2)}`;
        } catch (error) {
            currentBidInput.value = error.message;
        }
    };

    const profile = parseProfile();
    if (profile?.consumerNid && consumerNidInput) {
        consumerNidInput.value = profile.consumerNid;
    }

    loadProduct();
    loadCurrentBid();

    bidForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        showFeedback("Submitting bid…");
        const consumerNid = consumerNidInput?.value?.trim();
        const bidAmount = bidAmountInput?.value?.trim();
        if (!consumerNid || !bidAmount) {
            showFeedback("Consumer NID and bid amount are required.", "error");
            return;
        }
        const amountNumber = Number(bidAmount);
        if (Number.isNaN(amountNumber) || amountNumber <= 0) {
            showFeedback("Enter a valid positive amount.", "error");
            return;
        }
        const statusRaw = bidStatusInput?.value?.trim();
        const payload = {
            bidAmount: amountNumber,
            bidDate: bidDateInput?.value || null,
            bidStatus: statusRaw ? statusRaw.toUpperCase() : null
        };
        const query = new URLSearchParams({
            consumerNid,
            productId: productIdParam
        });
        try {
            await fetchJson(`/api/bids?${query.toString()}`, {
                method: "POST",
                body: JSON.stringify(payload)
            });
            showFeedback("Bid submitted successfully!", "success");
            bidForm.reset();
            loadCurrentBid();
        } catch (error) {
            showFeedback(error.message, "error");
        }
    });
})();
