(() => {
    const registerForm = document.getElementById("register-form");
    const loginForm = document.getElementById("login-form");
    const registerFeedback = document.getElementById("register-feedback");
    const loginFeedback = document.getElementById("login-feedback");
    const registerRoleSelect = document.getElementById("register-role");
    const loginRoleSelect = document.getElementById("login-role");
    const forgotPasswordTrigger = document.getElementById("forgot-password-trigger");
    const forgotPasswordPanel = document.getElementById("forgot-password-panel");
    const forgotPasswordForm = document.getElementById("forgot-password-form");
    const forgotPasswordFeedback = document.getElementById("forgot-password-feedback");
    const resetPasswordForm = document.getElementById("reset-password-form");
    const resetPasswordFeedback = document.getElementById("reset-password-feedback");
    const roleSections = document.querySelectorAll(".role-section");
    const currentYearEl = document.getElementById("current-year");
    const AUTH_EVENTS = ["heritagehubSession", "heritagehubProfile"];
    const signOutButtons = document.querySelectorAll('[data-action="sign-out"]');

    const parseStoredJson = (raw) => {
        if (!raw) return null;
        try {
            return JSON.parse(raw);
        } catch (err) {
            console.warn("Failed to parse stored role payload", err);
            return null;
        }
    };

    const clearAuthState = () => {
        localStorage.removeItem("heritagehubSession");
        localStorage.removeItem("heritagehubProfile");
        localStorage.removeItem("heritagehubCart");
    };

    const performSignOut = () => {
        clearAuthState();
        document.dispatchEvent(new CustomEvent("heritagehub:auth-changed"));
        updateRoleLinks();
        prefillRecoveryForms();
        window.location.href = "account.html";
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

    const prefillRecoveryForms = () => {
        const profile = parseStoredJson(localStorage.getItem("heritagehubProfile"));
        if (!profile) {
            return;
        }
        const role = profile.role ? profile.role.toString().toUpperCase() : null;
        const email = profile.email ?? profile.sellerEmail ?? profile.adminEmail ?? profile.consumerEmail ?? profile.username ?? profile.user;
        const forms = [
            { form: forgotPasswordForm, roleSelector: forgotPasswordForm?.querySelector('select[name="role"]'), emailInput: forgotPasswordForm?.querySelector('input[name="email"]') },
            { form: resetPasswordForm, roleSelector: resetPasswordForm?.querySelector('select[name="role"]'), emailInput: resetPasswordForm?.querySelector('input[name="email"]') }
        ];
        forms.forEach(({ roleSelector, emailInput }) => {
            if (roleSelector && role) {
                roleSelector.value = role;
            }
            if (emailInput && email) {
                emailInput.value = email;
            }
        });
    };

    updateRoleLinks();
    prefillRecoveryForms();
    document.addEventListener("heritagehub:auth-changed", () => {
        updateRoleLinks();
        prefillRecoveryForms();
    });
    window.addEventListener("storage", (event) => {
        if (AUTH_EVENTS.includes(event.key)) {
            updateRoleLinks();
            prefillRecoveryForms();
        }
    });

    if (currentYearEl) {
        currentYearEl.textContent = new Date().getFullYear();
    }

    const postJson = async (url, data) => {
        const bases = [];
        const currentOrigin = new URL(window.location.href).origin;
        bases.push(currentOrigin);
        if (!currentOrigin.endsWith(":8080")) {
            bases.push("http://localhost:8080");
            bases.push("http://127.0.0.1:8080");
        }
        let lastError = null;
        for (const base of bases) {
            try {
                const absolute = url.startsWith("http") ? url : `${base}${url}`;
                const response = await fetch(absolute, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(data)
                });
                const payload = await response.json().catch(() => ({}));
                if (!response.ok) {
                    const errorMessage = payload?.error || payload?.message || response.statusText;
                    throw new Error(errorMessage);
                }
                return payload;
            } catch (error) {
                lastError = error;
            }
        }
        if (lastError) {
            throw lastError;
        }
        throw new Error("Unable to reach the authentication service.");
    };

    const setFeedback = (element, message, type = "") => {
        if (!element) return;
        element.textContent = message;
        element.className = `auth-feedback ${type}`.trim();
    };

    const toggleRoleSections = () => {
        const role = registerRoleSelect?.value ?? "CONSUMER";
        roleSections.forEach(section => {
            const active = section.dataset.role === role;
            section.classList.toggle("hidden", !active);
        });
        registerForm?.querySelectorAll("[data-required]").forEach(field => {
            const roles = field.dataset.required
                .split(/\s+/)
                .filter(Boolean);
            const belongsToHiddenSection = field.closest(".role-section")?.classList.contains("hidden") ?? false;
            if (roles.includes(role) && !belongsToHiddenSection) {
                field.setAttribute("required", "required");
                return;
            }
            field.removeAttribute("required");
        });
    };

    registerRoleSelect?.addEventListener("change", () => {
        toggleRoleSections();
        setFeedback(registerFeedback, "");
    });
    toggleRoleSections();
    signOutButtons.forEach(button => {
        button.addEventListener("click", (event) => {
            event.preventDefault();
            performSignOut();
        });
    });

    forgotPasswordTrigger?.addEventListener("click", () => {
        if (!forgotPasswordPanel) {
            return;
        }
        const isHidden = forgotPasswordPanel.classList.toggle("hidden");
        if (!isHidden) {
            prefillRecoveryForms();
        }
        setFeedback(forgotPasswordFeedback, "");
        setFeedback(resetPasswordFeedback, "");
    });

    registerForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const role = registerRoleSelect?.value ?? "CONSUMER";
        setFeedback(registerFeedback, "Creating account…");
        const payload = buildRegisterPayload(role);
        try {
            const result = await postJson("/auth/register", payload);
            setFeedback(registerFeedback, "Account created successfully. You can now sign in.", "success");
            registerForm.reset();
            toggleRoleSections();
            if (result) {
                const { password, ...registrationData } = payload;
                const storedProfile = {
                    ...registrationData,
                    ...result,
                    role
                };
                localStorage.setItem("heritagehubProfile", JSON.stringify(storedProfile));
                document.dispatchEvent(new CustomEvent("heritagehub:auth-changed"));
                updateRoleLinks();
                prefillRecoveryForms();
            }
        } catch (error) {
            setFeedback(registerFeedback, error.message, "error");
        }
    });

    loginForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        setFeedback(loginFeedback, "Signing in…");
        const formData = new FormData(loginForm);
        const payload = {
            role: loginRoleSelect?.value ?? "CONSUMER",
            email: formData.get("email")?.toString().trim(),
            password: formData.get("password")?.toString()
        };
        try {
            const result = await postJson("/auth/login", payload);
            setFeedback(loginFeedback, "Login successful. Welcome back!", "success");
            if (result) {
                const storedSession = { ...result };
                storedSession.role = storedSession.role ?? (loginRoleSelect?.value ?? "CONSUMER");
                localStorage.setItem("heritagehubSession", JSON.stringify(storedSession));
                const existingProfile = parseStoredJson(localStorage.getItem("heritagehubProfile"));
                const sameRole = existingProfile?.role ? existingProfile.role.toString().toUpperCase() === storedSession.role.toString().toUpperCase() : true;
                const mergedProfile = {
                    ...(sameRole ? existingProfile : {}),
                    ...storedSession,
                    role: storedSession.role
                };
                localStorage.setItem("heritagehubProfile", JSON.stringify(mergedProfile));
                document.dispatchEvent(new CustomEvent("heritagehub:auth-changed"));
                updateRoleLinks();
                prefillRecoveryForms();
            }
        } catch (error) {
            setFeedback(loginFeedback, error.message, "error");
        }
    });

    forgotPasswordForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        setFeedback(forgotPasswordFeedback, "Sending verification code…");
        const formData = new FormData(forgotPasswordForm);
        const payload = {
            role: formData.get("role")?.toString().trim(),
            email: formData.get("email")?.toString().trim()
        };
        try {
            const result = await postJson("/auth/forgot-password", payload);
            const code = result?.code ? ` Use code ${result.code} to reset.` : "";
            setFeedback(forgotPasswordFeedback, `Verification code generated.${code}`, "success");
            if (resetPasswordForm) {
                const resetRole = resetPasswordForm.querySelector('select[name="role"]');
                const resetEmail = resetPasswordForm.querySelector('input[name="email"]');
                const resetCode = resetPasswordForm.querySelector('input[name="code"]');
                if (resetRole && payload.role) {
                    resetRole.value = payload.role.toUpperCase();
                }
                if (resetEmail && payload.email) {
                    resetEmail.value = payload.email;
                }
                if (resetCode && result?.code) {
                    resetCode.value = result.code;
                }
            }
        } catch (error) {
            setFeedback(forgotPasswordFeedback, error.message, "error");
        }
    });

    resetPasswordForm?.addEventListener("submit", async (event) => {
        event.preventDefault();
        setFeedback(resetPasswordFeedback, "Updating password…");
        const formData = new FormData(resetPasswordForm);
        const payload = {
            role: formData.get("role")?.toString().trim(),
            email: formData.get("email")?.toString().trim(),
            code: formData.get("code")?.toString().trim(),
            newPassword: formData.get("newPassword")?.toString()
        };
        try {
            await postJson("/auth/reset-password", payload);
            setFeedback(resetPasswordFeedback, "Password updated successfully. You can now sign in with your new password.", "success");
            setFeedback(loginFeedback, "Password updated. Please sign in with your new credentials.", "success");
            resetPasswordForm.reset();
            prefillRecoveryForms();
        } catch (error) {
            setFeedback(resetPasswordFeedback, error.message, "error");
        }
    });

    function buildRegisterPayload(role) {
        const section = registerForm?.querySelector(`.role-section[data-role="${role}"]`);
        const getValue = (name) => section?.querySelector(`[name="${name}"]`)?.value?.trim() ?? "";
        const getNullable = (name) => {
            const value = getValue(name);
            return value ? value : null;
        };
        const passwordValue = registerForm?.querySelector('input[name="password"]')?.value ?? "";
        const base = {
            role,
            email: getNullable("email"),
            password: passwordValue ? passwordValue.trim() : null,
            phoneNumber: getNullable("phoneNumber")
        };
        if (role === "SELLER") {
            const sellerPayload = {
                ...base,
                sellerNid: getValue("sellerNid") || null,
                sellerName: getValue("sellerName") || null,
                sellerFirstName: getNullable("sellerFirstName"),
                sellerLastName: getNullable("sellerLastName"),
                workingType: getNullable("workingType"),
                divisionName: getNullable("divisionName"),
                districtName: getNullable("districtName"),
                city: getNullable("city"),
                unionName: getNullable("unionName"),
                villageName: getNullable("villageName"),
                codeNo: getNullable("codeNo"),
                street: getNullable("street")
            };
            return sellerPayload;
        }
        // Consumer default
        return {
            ...base,
            consumerNid: getValue("consumerNid") || null,
            consumerFirstName: getNullable("consumerFirstName"),
            consumerLastName: getNullable("consumerLastName"),
            consumerName: getNullable("consumerName"),
            street: getNullable("street"),
            streetNo: getNullable("streetNo"),
            streetName: getNullable("streetName"),
            city: getNullable("city"),
            codeNo: getNullable("codeNo")
        };
    }
})();
