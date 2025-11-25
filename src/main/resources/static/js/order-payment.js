(() => {
    const paymentSummary = document.getElementById("bkash-payment-summary");
    const orderForm = document.getElementById("order-form");
    if (!paymentSummary || !orderForm) {
        return;
    }

    const updateSummary = () => {
        const medium = orderForm.querySelector('select[name="transactionMedium"]')?.value ?? "";
        const amountInput = orderForm.querySelector('input[name="transactionAmount"]');
        const referenceInput = orderForm.querySelector('input[name="transactionReference"]');
        const amount = amountInput?.value?.trim();

        if (medium === "BKASH" && amount) {
            paymentSummary.textContent = `Bkash: Tk ${Number(amount).toFixed(2)} will be processed after submission. Enter a transaction ID if available.`;
            paymentSummary.classList.remove("hidden");
        } else if (medium === "CASH") {
            paymentSummary.textContent = "Cash on delivery: collect the amount at delivery time.";
            paymentSummary.classList.remove("hidden");
        } else {
            paymentSummary.textContent = "";
            paymentSummary.classList.add("hidden");
        }
    };

    orderForm.addEventListener("input", updateSummary);
    updateSummary();
})();
