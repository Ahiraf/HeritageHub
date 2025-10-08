(() => {
    const productGridEl = document.getElementById("product-grid");
    const productEmptyStateEl = document.getElementById("product-empty-state");
    const productForm = document.getElementById("product-form");
    const productFeedback = document.getElementById("product-feedback");
    const refreshProductsBtn = document.getElementById("refresh-products");
    const productCountEl = document.getElementById("product-count");
    const currentYearEl = document.getElementById("current-year");

    const orderForm = document.getElementById("order-form");
    const orderFeedback = document.getElementById("order-feedback");

    const reviewForm = document.getElementById("review-form");
    const reviewFeedback = document.getElementById("review-feedback");

    const bidForm = document.getElementById("bid-form");
    const bidFeedback = document.getElementById("bid-feedback");
    const bidListEl = document.getElementById("bid-list");

    const fallbackImage = "images/hero.jpg";
    const CART_STORAGE_KEY = "heritagehubCart";
    const AUTH_EVENTS = ["heritagehubSession", "heritagehubProfile"];
    const productCache = new Map();

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
                    image: item.image || fallbackImage
                };
            })
            .filter(Boolean);
    };

    const parseStoredJson = (raw) => {
        if (!raw) return null;
        try {
            return JSON.parse(raw);
        } catch (err) {
            console.warn("Failed to parse stored payload", err);
            return null;
        }
    };

    const getStoredRole = () => {
        const session = parseStoredJson(localStorage.getItem("heritagehubSession"));
        if (session?.role) {
            return session.role.toString().toUpperCase();
        }
        const profile = parseStoredJson(localStorage.getItem("heritagehubProfile"));
        if (profile?.role) {
            return profile.role.toString().toUpperCase();
        }
        return null;
    };

    const updateRoleLinks = () => {
        const role = getStoredRole();
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

    updateRoleLinks();
    document.addEventListener("heritagehub:auth-changed", updateRoleLinks);
    document.addEventListener("heritagehub:cart-updated", refreshProductCartBadges);
    window.addEventListener("storage", (event) => {
        if (AUTH_EVENTS.includes(event.key)) {
            updateRoleLinks();
        }
    });

    const isSellerPage = document.body?.classList?.contains("seller-page");
    if (isSellerPage && getStoredRole() !== "SELLER") {
        const message = document.getElementById("seller-access-message");
        if (message) {
            message.textContent = "Seller access only. Please sign in as a seller.";
            message.className = "form-feedback error";
        } else {
            alert("Seller access only. Redirecting to the account page.");
        }
        setTimeout(() => {
            window.location.href = "account.html";
        }, 1500);
        return;
    }

    const readFileAsDataUrl = (file) => new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = () => reject(reader.error);
        reader.readAsDataURL(file);
    });

    const staticProducts = [
        {
            id: "HB-001",
            productName: "Handmade Jute Bag",
            category: "Accessories",
            saleType: "Fixed Price",
            craftType: "Jute Weaving",
            materialType: "Jute & Cotton",
            color: "Natural Beige",
            productPrice: 32.0,
            inStock: 18,
            biddable: false,
            weight: "450g",
            description: "A roomy handwoven jute bag finished with cotton lining and bamboo handles for everyday carry.",
            uploadImage: "images/bag.jpeg",
            sellerName: "Sultana Crafts"
        },
        {
            id: "HB-002",
            productName: "Jhuri Basket",
            category: "Home & Living",
            saleType: "Made to Order",
            craftType: "Bamboo Weaving",
            materialType: "Bamboo",
            color: "Warm Brown",
            productPrice: 22.0,
            inStock: 30,
            biddable: true,
            weight: "350g",
            description: "Lightweight bamboo storage basket ideal for fresh produce, picnic spreads, or decorative shelving.",
            uploadImage: "images/jhuri.jpeg",
            sellerName: "Bogura Basketry"
        },
        {
            id: "HB-003",
            productName: "Clay Pots",
            category: "Kitchenware",
            saleType: "Fixed Price",
            craftType: "Terracotta",
            materialType: "Earthen Clay",
            color: "Terracotta Red",
            productPrice: 28.5,
            inStock: 24,
            biddable: false,
            weight: "1.2kg",
            description: "Set of two traditional clay pots fired over wood to lock in earthy flavors for curries and desserts.",
            uploadImage: "images/clay-pots.jpeg",
            sellerName: "Rajshahi Potters"
        },
        {
            id: "HB-004",
            productName: "Handloom Saree",
            category: "Apparel",
            saleType: "Limited Edition",
            craftType: "Handloom Weaving",
            materialType: "Organic Cotton",
            color: "Crimson & Gold",
            productPrice: 78.0,
            inStock: 12,
            biddable: false,
            weight: "650g",
            description: "Luxurious six-yard saree woven on handheld looms with intricate anchol detailing in golden zari.",
            uploadImage: "images/saree.jpeg",
            sellerName: "Narayanganj Looms"
        },
        {
            id: "HB-005",
            productName: "Nakshi Kantha Throw",
            category: "Home Textiles",
            saleType: "Pre-Order",
            craftType: "Kantha Stitching",
            materialType: "Recycled Cotton",
            color: "Multicolor",
            productPrice: 64.0,
            inStock: 15,
            biddable: false,
            weight: "900g",
            description: "Layered kantha throw featuring village folklore motifs hand-stitched by the artisans of Faridpur.",
            uploadImage: "images/nakshi-kantha.jpeg",
            sellerName: "Faridpur Quilters"
        },
        {
            id: "HB-006",
            productName: "Brass Filigree Jewelry Set",
            category: "Jewelry",
            saleType: "Fixed Price",
            craftType: "Metal Filigree",
            materialType: "Polished Brass",
            color: "Antique Gold",
            productPrice: 48.0,
            inStock: 20,
            biddable: true,
            weight: "200g",
            description: "Delicate filigree necklace with matching earrings inspired by Mughal flower motifs, polished to a satin sheen.",
            uploadImage: "images/jewelry.jpeg",
            sellerName: "Dhakai Ornaments"
        },
        {
            id: "HB-007",
            productName: "Embroidered Pillow Cover",
            category: "Home Decor",
            saleType: "Fixed Price",
            craftType: "Hand Embroidery",
            materialType: "Cotton Canvas",
            color: "Ivory & Indigo",
            productPrice: 18.0,
            inStock: 40,
            biddable: false,
            weight: "180g",
            description: "18\" pillow cover accentuated with indigo floral embroidery and concealed zipper closure.",
            uploadImage: "images/pillow-cover.jpeg",
            sellerName: "Mymensingh Threads"
        },
        {
            id: "HB-008",
            productName: "Kantha Bed Cover",
            category: "Home Textiles",
            saleType: "Custom Size",
            craftType: "Kantha Stitching",
            materialType: "Handloom Cotton",
            color: "Earth Tones",
            productPrice: 120.0,
            inStock: 8,
            biddable: false,
            weight: "1.8kg",
            description: "Queen-size bed cover with layered kantha stitches forming geometric ripples for a cozy artisanal finish.",
            uploadImage: "images/bed-cover.jpeg",
            sellerName: "Khulna Stitchworks"
        }
    ];

    const getCart = () => {
        try {
            const raw = localStorage.getItem(CART_STORAGE_KEY);
            const parsed = raw ? JSON.parse(raw) : [];
            return normalizeCartItems(parsed);
        } catch (err) {
            console.warn("Failed to read cart from storage", err);
            return [];
        }
    };

    const saveCart = (cart) => {
        const normalized = normalizeCartItems(cart);
        localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(normalized));
        document.dispatchEvent(new CustomEvent("heritagehub:cart-updated", { detail: normalized }));
        return normalized;
    };

    const getProductId = (product) => {
        if (!product) {
            return null;
        }
        const raw = product.id ?? product.productName ?? product.uploadImage ?? product.materialType;
        return raw != null ? raw.toString() : null;
    };

    const updateCardCartState = (card, product) => {
        if (!card || !product) {
            return;
        }
        const productId = getProductId(product);
        if (!productId) {
            return;
        }
        const cart = getCart();
        const existing = cart.find(item => item.id === productId);
        const quantity = existing?.quantity ?? 0;
        const statusEl = card.querySelector(".cart-status");
        const button = card.querySelector(".add-to-cart-btn");
        if (button) {
            button.textContent = quantity > 0 ? `In cart (${quantity})` : "Add to Cart";
            button.classList.toggle("in-cart", quantity > 0);
        }
        if (statusEl) {
            if (quantity > 0) {
                statusEl.textContent = `Added to cart • Qty ${quantity}`;
                statusEl.classList.remove("hidden");
            } else {
                statusEl.textContent = "";
                statusEl.classList.add("hidden");
            }
        }
    };

    const refreshProductCartBadges = () => {
        if (!productGridEl) {
            return;
        }
        productGridEl.querySelectorAll(".product-card").forEach(card => {
            const productId = card.dataset.productId;
            if (!productId) {
                return;
            }
            const product = productCache.get(productId);
            if (product) {
                updateCardCartState(card, product);
            }
        });
    };

    const addToCart = (product, card) => {
        const productId = getProductId(product);
        if (!productId) {
            showFeedback(productFeedback, "Unable to add this product to the cart.", "error");
            return;
        }
        const cart = getCart();
        const name = product.productName ?? `Craft #${product.id ?? cart.length + 1}`;
        const numericPrice = Number(product.productPrice);
        const price = Number.isNaN(numericPrice) ? 0 : numericPrice;
        const existing = cart.find(item => item.id === productId);
        if (existing) {
            existing.quantity += 1;
        } else {
            cart.push({
                id: productId,
                name,
                price,
                image: product.uploadImage || fallbackImage,
                quantity: 1,
                biddable: Boolean(product.biddable)
            });
        }
        saveCart(cart);
        updateCardCartState(card, product);
        showFeedback(productFeedback, `${name} added to cart.`, "success");
    };

    const fetchJson = async (url, options = {}) => {
        const response = await fetch(url, {
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {})
            },
            ...options
        });
        const text = await response.text();
        let data = null;
        if (text) {
            try {
                data = JSON.parse(text);
            } catch (err) {
                console.warn("Failed to parse JSON response", err);
            }
        }
        if (!response.ok) {
            const message = data?.error || response.statusText || "Request failed";
            throw new Error(message);
        }
        return data;
    };

    const showFeedback = (element, message, type) => {
        if (!element) return;
        element.textContent = message;
        element.className = `form-feedback ${type ?? ""}`.trim();
    };

    const renderProducts = (products = []) => {
        if (!productGridEl || !productEmptyStateEl) {
            return;
        }
        productGridEl.innerHTML = "";
        productCache.clear();
        const hasProducts = Array.isArray(products) && products.length > 0;
        productEmptyStateEl.style.display = hasProducts ? "none" : "block";
        if (productCountEl) {
            productCountEl.textContent = hasProducts ? products.length : 0;
        }
        if (!hasProducts) {
            return;
        }

        products.forEach(product => {
            const card = document.createElement("article");
            card.className = "product-card";
            const productId = getProductId(product);
            if (productId) {
                card.dataset.productId = productId;
                productCache.set(productId, product);
            }

            const figure = document.createElement("figure");
            const img = document.createElement("img");
            img.src = product.uploadImage || fallbackImage;
            img.alt = product.productName ?? "Artisan product";
            img.loading = "lazy";
            figure.appendChild(img);
            card.appendChild(figure);

            const title = document.createElement("h3");
            title.textContent = product.productName ?? `Craft #${product.id}`;
            card.appendChild(title);

            const meta = document.createElement("p");
            meta.className = "meta";
            const category = product.category ? `Category: ${product.category}` : "Category: N/A";
            const saleType = product.saleType ? `Sale: ${product.saleType}` : null;
            const bidding = product.biddable ? "Bidding: Enabled" : null;
            meta.textContent = [category, saleType, bidding].filter(Boolean).join(" • ");
            card.appendChild(meta);

            if (product.productPrice != null) {
                const price = document.createElement("p");
                price.className = "price";
                const numericPrice = Number(product.productPrice);
                if (!Number.isNaN(numericPrice)) {
                    price.textContent = `Price: Tk ${numericPrice.toFixed(2)}`;
                } else {
                    price.textContent = `Price: Tk ${product.productPrice}`;
                }
                card.appendChild(price);
            }

            if (product.description) {
                const desc = document.createElement("p");
                desc.textContent = product.description;
                card.appendChild(desc);
            }

            const footer = document.createElement("footer");
            const stockText = document.createElement("span");
            stockText.textContent = `In stock: ${product.inStock ?? "N/A"}`;
            const sellerText = document.createElement("span");
            const sellerName = product.seller?.sellerNid ?? product.seller?.name ?? product.sellerName ?? product.seller ?? "Unknown";
            sellerText.textContent = `Seller: ${sellerName}`;
            footer.appendChild(stockText);
            footer.appendChild(sellerText);
            card.appendChild(footer);

            const actions = document.createElement("div");
            actions.className = "card-actions";
            const addToCartBtn = document.createElement("button");
            addToCartBtn.className = "btn subtle add-to-cart-btn";
            addToCartBtn.type = "button";
            addToCartBtn.textContent = "Add to Cart";
            addToCartBtn.addEventListener("click", () => addToCart(product, card));
            actions.appendChild(addToCartBtn);
            const status = document.createElement("span");
            status.className = "cart-status hidden";
            actions.appendChild(status);
            card.appendChild(actions);

            productGridEl.appendChild(card);
            updateCardCartState(card, product);
        });
    };

    const loadProducts = async () => {
        if (!productGridEl) {
            return;
        }
        try {
            const products = await fetchJson("/api/products");
            const combined = Array.isArray(products) && products.length > 0
                ? [...staticProducts, ...products]
                : staticProducts;
            renderProducts(combined);
        } catch (error) {
            showFeedback(productFeedback, error.message, "error");
            renderProducts(staticProducts);
        }
    };

    const loadRecentBids = async () => {
        if (!bidListEl) {
            return;
        }
        try {
            const bids = await fetchJson("/api/bids");
            bidListEl.innerHTML = "";
            if (!Array.isArray(bids) || bids.length === 0) {
                bidListEl.innerHTML = "<div class=\"list-item\">No bids yet.</div>";
                return;
            }
            bids
                .slice()
                .sort((a, b) => new Date(b.bidDate ?? 0) - new Date(a.bidDate ?? 0))
                .slice(0, 6)
                .forEach(bid => {
                    const item = document.createElement("div");
                    item.className = "list-item";
                    item.innerHTML = `
                        <strong>Product #${bid.product?.id ?? "?"}</strong>
                        <p>Amount: Tk ${Number(bid.bidAmount ?? 0).toFixed(2)} (${bid.bidStatus})</p>
                        <p class="meta">Consumer: ${bid.consumer?.consumerNid ?? "N/A"} • ${bid.bidDate?.replace("T", " ") ?? ""}</p>
                    `;
                    bidListEl.appendChild(item);
                });
        } catch (error) {
            showFeedback(bidFeedback, error.message, "error");
        }
    };

    productForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        showFeedback(productFeedback, "Saving product…", "");
        const formData = new FormData(productForm);
        const sellerNid = formData.get("sellerNid")?.toString().trim();
        if (!sellerNid) {
            showFeedback(productFeedback, "Seller NID is required.", "error");
            return;
        }
        const approvedById = formData.get("approvedById")?.toString().trim();
        const biddableValue = formData.get("isBiddable");
        const imageFile = productForm.querySelector('input[name="imageFile"]')?.files?.[0] ?? null;
        let uploadImage = formData.get("uploadImage")?.toString().trim() || null;
        if (imageFile) {
            try {
                uploadImage = await readFileAsDataUrl(imageFile);
            } catch (error) {
                showFeedback(productFeedback, "Failed to read the selected image. Please try again.", "error");
                return;
            }
        }
        const payload = {
            productName: formData.get("productName") || null,
            category: formData.get("category") || null,
            materialType: formData.get("materialType") || null,
            saleType: formData.get("saleType") || null,
            craftType: formData.get("craftType") || null,
            color: formData.get("color") || null,
            productPrice: formData.get("productPrice") ? Number(formData.get("productPrice")) : null,
            inStock: formData.get("inStock") ? Number(formData.get("inStock")) : null,
            size: formData.get("size") || null,
            weight: formData.get("weight") || null,
            productionTime: formData.get("productionTime") || null,
            description: formData.get("description") || null,
            uploadImage,
            biddable: biddableValue === "true"
        };

        const params = new URLSearchParams({ sellerNid });
        if (approvedById) {
            params.append("approvedById", approvedById);
        }

        try {
            await fetchJson(`/api/products?${params.toString()}`, {
                method: "POST",
                body: JSON.stringify(payload)
            });
            showFeedback(productFeedback, "Product saved successfully.", "success");
            productForm.reset();
            await loadProducts();
        } catch (error) {
            showFeedback(productFeedback, error.message, "error");
        }
    });

    orderForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        showFeedback(orderFeedback, "Submitting order…", "");
        const formData = new FormData(orderForm);
        const consumerNid = formData.get("consumerNid")?.toString().trim();
        const productId = formData.get("productId")?.toString().trim();
        if (!consumerNid || !productId) {
            showFeedback(orderFeedback, "Consumer NID and Product ID are required.", "error");
            return;
        }

        const payload = {
            productWeight: formData.get("productWeight") || null,
            productQuantity: formData.get("productQuantity") ? Number(formData.get("productQuantity")) : null,
            totalPrice: formData.get("totalPrice") ? Number(formData.get("totalPrice")) : null,
            orderType: formData.get("orderType") || null,
            deliveryCharge: formData.get("deliveryCharge") ? Number(formData.get("deliveryCharge")) : null
        };

        const params = new URLSearchParams({
            consumerNid,
            productId
        });

        try {
            await fetchJson(`/api/orders?${params.toString()}`, {
                method: "POST",
                body: JSON.stringify(payload)
            });
            showFeedback(orderFeedback, "Order created successfully.", "success");
            orderForm.reset();
        } catch (error) {
            showFeedback(orderFeedback, error.message, "error");
        }
    });

    reviewForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        showFeedback(reviewFeedback, "Posting review…", "");
        const formData = new FormData(reviewForm);
        const consumerNid = formData.get("consumerNid")?.toString().trim();
        const productId = formData.get("productId")?.toString().trim();
        if (!consumerNid || !productId) {
            showFeedback(reviewFeedback, "Consumer NID and Product ID are required.", "error");
            return;
        }

        const payload = {
            rating: formData.get("rating") ? Number(formData.get("rating")) : null,
            comment: formData.get("comment") || null
        };

        const params = new URLSearchParams({
            consumerNid,
            productId
        });

        try {
            await fetchJson(`/api/reviews?${params.toString()}`, {
                method: "POST",
                body: JSON.stringify(payload)
            });
            showFeedback(reviewFeedback, "Review posted successfully.", "success");
            reviewForm.reset();
        } catch (error) {
            showFeedback(reviewFeedback, error.message, "error");
        }
    });

    bidForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        showFeedback(bidFeedback, "Submitting bid…", "");
        const formData = new FormData(bidForm);
        const consumerNid = formData.get("consumerNid")?.toString().trim();
        const productId = formData.get("productId")?.toString().trim();
        const amount = formData.get("bidAmount")?.toString().trim();
        if (!consumerNid || !productId || !amount) {
            showFeedback(bidFeedback, "All fields are required.", "error");
            return;
        }

        const payload = {
            bidAmount: Number(amount)
        };

        const params = new URLSearchParams({
            consumerNid,
            productId
        });

        try {
            await fetchJson(`/api/bids?${params.toString()}`, {
                method: "POST",
                body: JSON.stringify(payload)
            });
            showFeedback(bidFeedback, "Bid submitted successfully.", "success");
            bidForm.reset();
            await loadRecentBids();
        } catch (error) {
            showFeedback(bidFeedback, error.message, "error");
        }
    });

    refreshProductsBtn?.addEventListener("click", loadProducts);

    if (currentYearEl) {
        currentYearEl.textContent = new Date().getFullYear();
    }

    if (productGridEl) {
        loadProducts();
    }
    if (bidListEl) {
        loadRecentBids();
    }
})();
