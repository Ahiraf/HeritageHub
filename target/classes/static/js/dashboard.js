(() => {
    const sections = document.querySelectorAll(".dashboard-section");
    const feedbackEl = document.getElementById("dashboard-feedback");
    const currentYearEl = document.getElementById("current-year");
    const CART_STORAGE_KEY = "heritagehubCart";
    const AUTH_EVENTS = ["heritagehubSession", "heritagehubProfile"];

    if (currentYearEl) {
        currentYearEl.textContent = new Date().getFullYear();
    }

    const fetchJson = async (url) => {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(response.statusText || "Request failed");
        }
        return response.json();
    };

    const normalizeCartItems = (items = []) => {
        if (!Array.isArray(items)) {
            return [];
        }
        return items
            .map(item => {
                if (!item) {
                    return null;
                }
                const id = item.id != null ? item.id.toString() : null;
                if (!id) {
                    return null;
                }
                const quantityValue = Number(item.quantity);
                const priceValue = Number(item.price);
                return {
                    ...item,
                    id,
                    quantity: Number.isFinite(quantityValue) && quantityValue > 0 ? quantityValue : 1,
                    price: Number.isFinite(priceValue) && priceValue >= 0 ? priceValue : 0,
                    image: item.image || null
                };
            })
            .filter(Boolean);
    };

    const readCart = () => {
        try {
            const raw = localStorage.getItem(CART_STORAGE_KEY);
            const parsed = raw ? JSON.parse(raw) : [];
            return normalizeCartItems(parsed);
        } catch (err) {
            console.warn("Failed to parse cart from storage", err);
            return [];
        }
    };

    const writeCart = (cart) => {
        const normalized = normalizeCartItems(cart);
        localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(normalized));
        document.dispatchEvent(new CustomEvent("heritagehub:cart-updated", { detail: normalized }));
        return normalized;
    };

    const adjustCartItem = (productId, delta) => {
        if (!productId) {
            return;
        }
        const cart = readCart();
        const index = cart.findIndex(item => item.id === productId);
        if (index === -1) {
            return;
        }
        const current = cart[index];
        const nextQuantity = (current.quantity ?? 1) + delta;
        if (nextQuantity <= 0) {
            cart.splice(index, 1);
        } else {
            cart[index] = { ...current, quantity: nextQuantity };
        }
        writeCart(cart);
    };

    const removeCartItem = (productId) => {
        if (!productId) {
            return;
        }
        const cart = readCart().filter(item => item.id !== productId);
        writeCart(cart);
    };

    const updateFeedback = (message, type = "") => {
        if (!feedbackEl) return;
        feedbackEl.textContent = message;
        feedbackEl.className = `auth-feedback ${type}`.trim();
    };

    const detectRole = () => {
        const sessionRaw = localStorage.getItem("heritagehubSession");
        const profileRaw = localStorage.getItem("heritagehubProfile");
        const parse = (raw) => {
            if (!raw) return null;
            try {
                return JSON.parse(raw);
            } catch (err) {
                console.warn("Failed to parse stored profile", err);
                return null;
            }
        };
        const session = parse(sessionRaw);
        if (session?.role) {
            return session.role.toString().toUpperCase();
        }
        const profile = parse(profileRaw);
        if (profile?.role) {
            return profile.role.toString().toUpperCase();
        }
        return null;
    };

    const updateRoleLinks = () => {
        const role = detectRole();
        document.querySelectorAll("[data-role-link]").forEach(element => {
            const raw = element.dataset.roleLink ?? "";
            const roles = raw
                .split(/\s+/)
                .filter(Boolean)
                .map(value => value.toUpperCase());
            const hasAny = roles.includes("ANY");
            const shouldShow = role ? (hasAny || roles.includes(role)) : false;
            element.classList.toggle("hidden", !shouldShow);
        });
    };

    const showSection = (role) => {
        let visible = false;
        sections.forEach(section => {
            const isMatch = section.dataset.role === role;
            section.classList.toggle("hidden", !isMatch);
            if (isMatch) {
                visible = true;
            }
        });
        return visible;
    };

    const hydrateSellerCards = async () => {
        const stats = {
            ordersToday: 0,
            ordersMonth: 0,
            topProduct: "N/A",
            earningsToday: 0,
            earningsMonth: 0,
            payoutStatus: "Pending"
        };
        try {
            const orders = await fetchJson("/api/orders");
            if (Array.isArray(orders)) {
                const now = new Date();
                const todayStr = now.toISOString().slice(0, 10);
                stats.ordersToday = orders.filter(order => (order.orderDate ?? "").startsWith(todayStr)).length;
                stats.ordersMonth = orders.filter(order => {
                    const orderDate = order.orderDate ?? "";
                    return orderDate.slice(0, 7) === todayStr.slice(0, 7);
                }).length;
            }
        } catch (err) {
            console.warn("Unable to load order stats", err);
        }
        try {
            const products = await fetchJson("/api/products");
            if (Array.isArray(products) && products.length > 0) {
                stats.topProduct = products[0]?.productName ?? "Featured Craft";
            }
        } catch (err) {
            console.warn("Unable to load product stats", err);
        }

        document.querySelector('[data-stat="orders-today"]')?.textContent = stats.ordersToday;
        document.querySelector('[data-stat="orders-month"]')?.textContent = stats.ordersMonth;
        document.querySelector('[data-stat="top-product"]')?.textContent = stats.topProduct;
        document.querySelector('[data-stat="earnings-today"]')?.textContent = stats.earningsToday.toFixed(2);
        document.querySelector('[data-stat="earnings-month"]')?.textContent = stats.earningsMonth.toFixed(2);
        document.querySelector('[data-stat="payout-status"]')?.textContent = stats.payoutStatus;
    };

    const hydrateAdminCards = async () => {
        const counts = {
            sellers: 0,
            consumers: 0,
            approvals: 0,
            listingQueue: 0,
            complaints: 0
        };
        try {
            const sellers = await fetchJson("/api/sellers");
            if (Array.isArray(sellers)) {
                counts.sellers = sellers.length;
                counts.approvals = sellers.filter(seller => !seller.approvedBy).length;
            }
        } catch (err) {
            console.warn("Unable to load sellers", err);
        }
        try {
            const consumers = await fetchJson("/api/consumers");
            if (Array.isArray(consumers)) {
                counts.consumers = consumers.length;
            }
        } catch (err) {
            console.warn("Unable to load consumers", err);
        }
        try {
            const products = await fetchJson("/api/products");
            if (Array.isArray(products)) {
                counts.listingQueue = products.filter(product => !product.approvedBy).length;
            }
        } catch (err) {
            console.warn("Unable to load products", err);
        }

        document.querySelector('[data-stat="seller-count"]')?.textContent = counts.sellers;
        document.querySelector('[data-stat="consumer-count"]')?.textContent = counts.consumers;
        document.querySelector('[data-stat="pending-approvals"]')?.textContent = counts.approvals;
        document.querySelector('[data-stat="listing-queue"]')?.textContent = counts.listingQueue;
        document.querySelector('[data-stat="complaints-open"]')?.textContent = counts.complaints;
    };

    const renderCart = () => {
        const container = document.querySelector('[data-list="cart"]');
        if (!container) {
            return;
        }
        const cart = readCart();
        container.innerHTML = "";
        if (!Array.isArray(cart) || cart.length === 0) {
            container.innerHTML = "<p class=\"empty-state\">Your cart is empty. Add crafts from the marketplace to see them here.</p>";
            return;
        }
        let total = 0;
        cart.forEach(item => {
            const quantity = Number(item.quantity) || 1;
            const price = Number(item.price) || 0;
            const lineTotal = price * quantity;
            total += lineTotal;
            const entry = document.createElement("div");
            entry.className = "list-item cart-item";
            entry.innerHTML = `
                <div class="cart-item-header">
                    <strong>${item.name}</strong>
                    <span class="cart-item-price">Tk ${price.toFixed(2)} each</span>
                </div>
                <div class="cart-controls">
                    <button type="button" class="cart-btn" data-action="decrease" aria-label="Decrease quantity">−</button>
                    <span class="cart-qty" aria-live="polite">${quantity}</span>
                    <button type="button" class="cart-btn" data-action="increase" aria-label="Increase quantity">+</button>
                    <button type="button" class="cart-btn cart-remove" data-action="remove">Remove</button>
                </div>
                <p class="meta">Line total: Tk ${lineTotal.toFixed(2)}${item.biddable ? " • Bidding item" : ""}</p>
            `;
            container.appendChild(entry);
            const increaseBtn = entry.querySelector('[data-action="increase"]');
            const decreaseBtn = entry.querySelector('[data-action="decrease"]');
            const removeBtn = entry.querySelector('[data-action="remove"]');
            const id = item.id;
            increaseBtn?.addEventListener("click", () => adjustCartItem(id, 1));
            decreaseBtn?.addEventListener("click", () => adjustCartItem(id, -1));
            removeBtn?.addEventListener("click", () => removeCartItem(id));
        });
        const summary = document.createElement("div");
        summary.className = "list-item cart-summary";
        summary.innerHTML = `<strong>Total: Tk ${total.toFixed(2)}</strong>`;
        container.appendChild(summary);
    };

    const bindActions = () => {
        document.querySelectorAll("[data-action]").forEach(button => {
            button.addEventListener("click", () => {
                const action = button.dataset.action;
                const message = {
                    "open-stock": "Stock adjustment workflows coming soon. Use the Marketplace Console for now.",
                    "manage-payments": "Payment management will connect to transactions once configured.",
                    "open-approvals": "Approvals dashboard will surface pending sellers after integration.",
                    "manage-complaints": "Complaint management tools are on the roadmap.",
                    "checkout-cart": "Checkout flow is coming soon. For now, contact the artisan directly after placing orders."
                }[action] ?? "Feature under development.";
                alert(message);
            });
        });
    };

    const bootstrap = async () => {
        updateRoleLinks();
        document.addEventListener("heritagehub:auth-changed", updateRoleLinks);
        window.addEventListener("storage", (event) => {
            if (AUTH_EVENTS.includes(event.key)) {
                updateRoleLinks();
            }
        });

        const role = detectRole();
        if (!role) {
            updateFeedback("Sign in first so we know which dashboard to display.");
            sections.forEach(section => section.classList.add("hidden"));
            return;
        }

        updateFeedback(`Showing ${role.toLowerCase()} workspace.`, "success");
        const sectionFound = showSection(role);
        if (!sectionFound) {
            updateFeedback("No dashboard configured for your role yet.", "error");
            return;
        }

        bindActions();
        if (role === "SELLER") {
            await hydrateSellerCards();
        } else if (role === "ADMIN") {
            await hydrateAdminCards();
        } else if (role === "CONSUMER") {
            renderCart();
        }
    };

    document.addEventListener("heritagehub:cart-updated", renderCart);
    bootstrap();
})();
