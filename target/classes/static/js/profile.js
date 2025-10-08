(() => {
    const profileContentEl = document.getElementById("profile-content");
    const profileFeedbackEl = document.getElementById("profile-feedback");
    const currentYearEl = document.getElementById("current-year");

    if (currentYearEl) {
        currentYearEl.textContent = new Date().getFullYear();
    }

    const parseStoredJson = (raw) => {
        if (!raw) return null;
        try {
            return JSON.parse(raw);
        } catch (err) {
            console.warn("Failed to parse stored profile payload", err);
            return null;
        }
    };

    const resolveProfile = () => {
        const profile = parseStoredJson(localStorage.getItem("heritagehubProfile"));
        if (profile?.role) {
            return profile;
        }
        const session = parseStoredJson(localStorage.getItem("heritagehubSession"));
        return session ?? null;
    };

    const formatValue = (value) => {
        if (value === null || value === undefined) {
            return "Not provided";
        }
        if (typeof value === "string" && value.trim() === "") {
            return "Not provided";
        }
        if (typeof value === "string") {
            const trimmed = value.trim();
            return trimmed === "" ? "Not provided" : trimmed;
        }
        if (Array.isArray(value)) {
            return value.length > 0 ? value.join(", ") : "Not provided";
        }
        return value;
    };

    const renderProfile = (profile) => {
        if (!profileContentEl || !profileFeedbackEl) {
            return;
        }
        const role = profile.role ? profile.role.toString().toUpperCase() : null;
        if (!role) {
            profileFeedbackEl.textContent = "We could not detect your role. Please sign in again.";
            profileFeedbackEl.className = "form-feedback error";
            return;
        }

        const fieldTemplates = {
            CONSUMER: [
                { label: "Role", keys: ["role"], transform: value => value ? value.toString().charAt(0).toUpperCase() + value.toString().slice(1).toLowerCase() : value },
                { label: "Consumer NID", keys: ["consumerNid"] },
                { label: "Full Name", keys: ["consumerName"] },
                { label: "Email", keys: ["email"] },
                { label: "Phone", keys: ["phoneNumber"] },
                { label: "Street / Holding", keys: ["street"] },
                { label: "Street Name", keys: ["streetName"] },
                { label: "House No.", keys: ["streetNo"] },
                { label: "City / Upazila", keys: ["city"] },
                { label: "Postal Code", keys: ["codeNo"] }
            ],
            SELLER: [
                { label: "Role", keys: ["role"], transform: value => value ? value.toString().charAt(0).toUpperCase() + value.toString().slice(1).toLowerCase() : value },
                { label: "Seller NID", keys: ["sellerNid"] },
                { label: "Business Name", keys: ["sellerName"] },
                { label: "Contact First Name", keys: ["sellerFirstName", "firstName"] },
                { label: "Contact Last Name", keys: ["sellerLastName", "lastName"] },
                { label: "Email", keys: ["email"] },
                { label: "Phone", keys: ["phoneNumber"] },
                { label: "Working Type", keys: ["workingType"] },
                { label: "Division", keys: ["divisionName", "division"] },
                { label: "District", keys: ["districtName", "district"] },
                { label: "Upazila / City", keys: ["city", "upazila"] },
                { label: "Union", keys: ["unionName"] },
                { label: "Village", keys: ["villageName"] },
                { label: "Postal Code", keys: ["codeNo", "postCode"] },
                { label: "Business Address", keys: ["sellerAddress", "address", "street"] },
                { label: "Manager / Approver ID", keys: ["managerId", "approvedById"] }
            ],
            ADMIN: [
                { label: "Role", keys: ["role"], transform: value => value ? value.toString().charAt(0).toUpperCase() + value.toString().slice(1).toLowerCase() : value },
                { label: "Admin ID", keys: ["adminId", "id"] },
                { label: "Admin Name", keys: ["adminName"] },
                { label: "Email", keys: ["email"] },
                { label: "Phone", keys: ["phoneNumber"] },
                { label: "Title", keys: ["adminRole"] }
            ]
        };

        const templates = fieldTemplates[role];
        if (!templates) {
            profileFeedbackEl.textContent = `No profile template configured for role ${role}.`;
            profileFeedbackEl.className = "form-feedback error";
            return;
        }

        profileContentEl.innerHTML = "";
        templates.forEach(field => {
            const value = field.keys
                .map(key => profile[key])
                .find(entry => entry !== null && entry !== undefined && `${entry}`.trim() !== "");
            const finalValue = field.transform ? field.transform(value) : value;
            const card = document.createElement("div");
            card.className = "profile-item";
            const heading = document.createElement("h3");
            heading.textContent = field.label;
            const paragraph = document.createElement("p");
            paragraph.textContent = formatValue(finalValue);
            card.appendChild(heading);
            card.appendChild(paragraph);
            profileContentEl.appendChild(card);
        });

        profileFeedbackEl.textContent = `Showing details for your ${role.toLowerCase()} account.`;
        profileFeedbackEl.className = "form-feedback success";
        profileContentEl.classList.remove("hidden");
    };

    const showNoProfileMessage = () => {
        if (!profileFeedbackEl || !profileContentEl) {
            return;
        }
        profileFeedbackEl.textContent = "Sign in to view your profile.";
        profileFeedbackEl.className = "form-feedback error";
        profileContentEl.classList.add("hidden");
        profileContentEl.innerHTML = "";
    };

    const loadProfile = () => {
        const profile = resolveProfile();
        if (!profile) {
            showNoProfileMessage();
            return;
        }
        renderProfile(profile);
    };

    document.addEventListener("heritagehub:auth-changed", loadProfile);
    window.addEventListener("storage", loadProfile);

    loadProfile();
})();
