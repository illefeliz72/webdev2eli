const API_BASE = "";

let ownersCache = [];
let petsCache = [];

// Generic request helper supporting 204 No Content & structured errors
async function request(path, options = {}) {
    let response;
    try {
        response = await fetch(`${API_BASE}${path}`, options);
    } catch (networkErr) {
        const err = new Error("Network error: backend unreachable. Start the server and retry.");
        err.status = 0;
        throw err;
    }

    const type = response.headers.get("content-type") || "";
    const body = response.status === 204
        ? null
        : type.includes("application/json")
            ? await response.json()
            : await response.text();

    if (!response.ok) {
        let message = `Request failed (${response.status})`;
        if (typeof body === "object" && body !== null) {
            message = body.message || message;
            if (Array.isArray(body.errors) && body.errors.length > 0) {
                const details = body.errors.map(e => `${e.field}: ${e.message}`).join("; ");
                message = `${message} (${details})`;
            }
        }
        const error = new Error(message);
        error.status = response.status;
        error.details = body;
        throw error;
    }

    return body;
}

function showBanner(message, isError = false) {
    const banner = document.getElementById("statusBanner");
    banner.textContent = message;
    banner.className = `banner ${isError ? "error" : "success"}`;
}

function clearBanner() {
    const banner = document.getElementById("statusBanner");
    banner.textContent = "";
    banner.className = "banner hidden";
}

// --- LOADERS & DROPDOWN POPULATION ---

async function loadOwners() {
    ownersCache = await request("/api/owners");
    renderOwnersTable(ownersCache);
    populateOwnerDropdowns(ownersCache);
}

async function loadPets(speciesFilter = "") {
    const query = speciesFilter ? `?species=${encodeURIComponent(speciesFilter)}` : "";
    petsCache = await request(`/api/pets${query}`);
    renderPetsTable(petsCache);
    populateAvailablePetsDropdown(petsCache);
}

async function loadAdoptions() {
    const adoptions = await request("/api/adoptions");
    renderAdoptionsTable(adoptions);
}

async function refreshAll() {
    try {
        await Promise.all([
            loadOwners(),
            loadPets(document.getElementById("speciesFilter").value.trim()),
            loadAdoptions()
        ]);
    } catch (err) {
        showBanner(err.message, true);
    }
}

function populateOwnerDropdowns(owners) {
    const petOwnerSelect = document.getElementById("petOwnerSelect");
    const adoptOwnerSelect = document.getElementById("adoptOwnerSelect");

    const currentPetOwner = petOwnerSelect.value;
    petOwnerSelect.innerHTML = '<option value="">-- Unassigned (Available) --</option>';
    adoptOwnerSelect.innerHTML = '<option value="">-- Select Owner --</option>';

    owners.forEach(owner => {
        const opt1 = document.createElement("option");
        opt1.value = String(owner.id);
        opt1.textContent = `${owner.fullName} (${owner.email})`;
        petOwnerSelect.appendChild(opt1);

        const opt2 = document.createElement("option");
        opt2.value = String(owner.id);
        opt2.textContent = `${owner.fullName} (#${owner.id})`;
        adoptOwnerSelect.appendChild(opt2);
    });

    if (currentPetOwner) {
        petOwnerSelect.value = currentPetOwner;
    }
}

function populateAvailablePetsDropdown(pets) {
    const adoptPetSelect = document.getElementById("adoptPetSelect");
    adoptPetSelect.innerHTML = '<option value="">-- Select Available Pet --</option>';

    pets.filter(p => p.status === "AVAILABLE").forEach(pet => {
        const opt = document.createElement("option");
        opt.value = String(pet.id);
        opt.textContent = `#${pet.id} - ${pet.name} (${pet.species}, ${pet.microchipNumber})`;
        adoptPetSelect.appendChild(opt);
    });
}

// --- TABLE RENDERERS ---

function renderPetsTable(pets) {
    const tbody = document.getElementById("petsTableBody");
    tbody.innerHTML = "";

    if (pets.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 8;
        td.textContent = "No pets found.";
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    pets.forEach(pet => {
        const tr = document.createElement("tr");

        const cells = [
            pet.id,
            pet.microchipNumber,
            pet.name,
            pet.species,
            pet.breed || "-",
            null,
            pet.ownerName ? `${pet.ownerName} (#${pet.ownerId})` : "Unassigned"
        ];

        cells.forEach((val, idx) => {
            const td = document.createElement("td");
            if (idx === 5) {
                const badge = document.createElement("span");
                badge.className = `badge ${pet.status === "ADOPTED" ? "badge-adopted" : "badge-available"}`;
                badge.textContent = pet.status;
                td.appendChild(badge);
            } else {
                td.textContent = String(val);
            }
            tr.appendChild(td);
        });

        const actionTd = document.createElement("td");
        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.className = "btn-secondary";
        editBtn.textContent = "Edit";
        editBtn.style.marginRight = "0.35rem";
        editBtn.addEventListener("click", () => startEditPet(pet));

        const delBtn = document.createElement("button");
        delBtn.type = "button";
        delBtn.className = "btn-danger";
        delBtn.textContent = "Delete";
        delBtn.addEventListener("click", () => handleDeletePet(pet));

        actionTd.appendChild(editBtn);
        actionTd.appendChild(delBtn);
        tr.appendChild(actionTd);
        tbody.appendChild(tr);
    });
}

function renderOwnersTable(owners) {
    const tbody = document.getElementById("ownersTableBody");
    tbody.innerHTML = "";

    if (owners.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 5;
        td.textContent = "No owners registered.";
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    owners.forEach(owner => {
        const tr = document.createElement("tr");
        [owner.id, owner.fullName, owner.email, owner.phone || "-"].forEach(val => {
            const td = document.createElement("td");
            td.textContent = String(val);
            tr.appendChild(td);
        });

        const actionTd = document.createElement("td");
        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.className = "btn-secondary";
        editBtn.textContent = "Edit";
        editBtn.style.marginRight = "0.35rem";
        editBtn.addEventListener("click", () => startEditOwner(owner));

        const delBtn = document.createElement("button");
        delBtn.type = "button";
        delBtn.className = "btn-danger";
        delBtn.textContent = "Delete";
        delBtn.addEventListener("click", () => handleDeleteOwner(owner));

        actionTd.appendChild(editBtn);
        actionTd.appendChild(delBtn);
        tr.appendChild(actionTd);
        tbody.appendChild(tr);
    });
}

function renderAdoptionsTable(adoptions) {
    const tbody = document.getElementById("adoptionsTableBody");
    tbody.innerHTML = "";

    if (adoptions.length === 0) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 5;
        td.textContent = "No adoptions recorded yet.";
        tr.appendChild(td);
        tbody.appendChild(tr);
        return;
    }

    adoptions.forEach(a => {
        const tr = document.createElement("tr");
        [
            a.id,
            `${a.petName} (#${a.petId})`,
            `${a.ownerName} (#${a.ownerId})`,
            a.adoptedAt ? a.adoptedAt.replace("T", " ").slice(0, 19) : "-",
            a.notes || "-"
        ].forEach(val => {
            const td = document.createElement("td");
            td.textContent = String(val);
            tr.appendChild(td);
        });
        tbody.appendChild(tr);
    });
}

// --- PET FORM HANDLERS ---

function startEditPet(pet) {
    document.getElementById("petEditId").value = pet.id;
    document.getElementById("petMicrochip").value = pet.microchipNumber;
    document.getElementById("petName").value = pet.name;
    document.getElementById("petSpecies").value = pet.species;
    document.getElementById("petBreed").value = pet.breed || "";
    document.getElementById("petOwnerSelect").value = pet.ownerId ? String(pet.ownerId) : "";
    document.getElementById("petStatus").value = pet.status || "AVAILABLE";
    document.getElementById("petSubmitBtn").textContent = `Update Pet #${pet.id}`;
    document.getElementById("petCancelBtn").classList.remove("hidden");
    clearBanner();
}

function resetPetForm() {
    document.getElementById("petForm").reset();
    document.getElementById("petEditId").value = "";
    document.getElementById("petSubmitBtn").textContent = "Save Pet";
    document.getElementById("petCancelBtn").classList.add("hidden");
}

document.getElementById("petCancelBtn").addEventListener("click", () => {
    resetPetForm();
    clearBanner();
});

document.getElementById("petForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const submitBtn = document.getElementById("petSubmitBtn");
    submitBtn.disabled = true;

    const editId = document.getElementById("petEditId").value;
    const ownerVal = document.getElementById("petOwnerSelect").value;

    const payload = {
        microchipNumber: document.getElementById("petMicrochip").value.trim(),
        name: document.getElementById("petName").value.trim(),
        species: document.getElementById("petSpecies").value.trim(),
        breed: document.getElementById("petBreed").value.trim(),
        ownerId: ownerVal ? Number(ownerVal) : null
    };

    if (editId) {
        payload.status = document.getElementById("petStatus").value;
    }

    try {
        if (editId) {
            await request(`/api/pets/${editId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            showBanner(`Pet #${editId} updated successfully.`);
        } else {
            const created = await request("/api/pets", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            showBanner(`Pet #${created.id} (${created.name}) created successfully.`);
        }
        resetPetForm();
        await refreshAll();
    } catch (err) {
        showBanner(`Error (${err.status || "Network"}): ${err.message}`, true);
    } finally {
        submitBtn.disabled = false;
    }
});

async function handleDeletePet(pet) {
    if (!confirm(`Delete Pet #${pet.id} "${pet.name}" (${pet.microchipNumber})?`)) {
        return;
    }
    try {
        await request(`/api/pets/${pet.id}`, { method: "DELETE" });
        showBanner(`Pet #${pet.id} (${pet.name}) deleted.`);
        if (document.getElementById("petEditId").value === String(pet.id)) {
            resetPetForm();
        }
        await refreshAll();
    } catch (err) {
        showBanner(`Delete failed (${err.status || "Network"}): ${err.message}`, true);
    }
}

// --- OWNER FORM HANDLERS ---

function startEditOwner(owner) {
    document.getElementById("ownerEditId").value = owner.id;
    document.getElementById("ownerFullName").value = owner.fullName;
    document.getElementById("ownerEmail").value = owner.email;
    document.getElementById("ownerPhone").value = owner.phone || "";
    document.getElementById("ownerSubmitBtn").textContent = `Update Owner #${owner.id}`;
    document.getElementById("ownerCancelBtn").classList.remove("hidden");
    clearBanner();
}

function resetOwnerForm() {
    document.getElementById("ownerForm").reset();
    document.getElementById("ownerEditId").value = "";
    document.getElementById("ownerSubmitBtn").textContent = "Save Owner";
    document.getElementById("ownerCancelBtn").classList.add("hidden");
}

document.getElementById("ownerCancelBtn").addEventListener("click", () => {
    resetOwnerForm();
    clearBanner();
});

document.getElementById("ownerForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const submitBtn = document.getElementById("ownerSubmitBtn");
    submitBtn.disabled = true;

    const editId = document.getElementById("ownerEditId").value;
    const payload = {
        fullName: document.getElementById("ownerFullName").value.trim(),
        email: document.getElementById("ownerEmail").value.trim(),
        phone: document.getElementById("ownerPhone").value.trim()
    };

    try {
        if (editId) {
            await request(`/api/owners/${editId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            showBanner(`Owner #${editId} updated successfully.`);
        } else {
            const created = await request("/api/owners", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            showBanner(`Owner #${created.id} (${created.fullName}) created successfully.`);
        }
        resetOwnerForm();
        await refreshAll();
    } catch (err) {
        showBanner(`Error (${err.status || "Network"}): ${err.message}`, true);
    } finally {
        submitBtn.disabled = false;
    }
});

async function handleDeleteOwner(owner) {
    if (!confirm(`Delete Owner #${owner.id} "${owner.fullName}" (${owner.email})?`)) {
        return;
    }
    try {
        await request(`/api/owners/${owner.id}`, { method: "DELETE" });
        showBanner(`Owner #${owner.id} (${owner.fullName}) deleted.`);
        if (document.getElementById("ownerEditId").value === String(owner.id)) {
            resetOwnerForm();
        }
        await refreshAll();
    } catch (err) {
        showBanner(`Delete failed (${err.status || "Network"}): ${err.message}`, true);
    }
}

// --- ATOMIC WORKFLOW: ADOPT A PET ---

document.getElementById("adoptionForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const submitBtn = document.getElementById("adoptSubmitBtn");
    submitBtn.disabled = true;

    const payload = {
        petId: Number(document.getElementById("adoptPetSelect").value),
        ownerId: Number(document.getElementById("adoptOwnerSelect").value),
        notes: document.getElementById("adoptNotes").value.trim(),
        simulateFailureAfterPetUpdate: document.getElementById("simulateRollback").checked
    };

    try {
        const result = await request("/api/adoptions", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        showBanner(`Adoption #${result.id} complete! ${result.petName} is now adopted by ${result.ownerName}.`);
        document.getElementById("adoptionForm").reset();
        await refreshAll();
    } catch (err) {
        showBanner(`Adoption failed & rolled back (${err.status || "Network"}): ${err.message}`, true);
        await refreshAll();
    } finally {
        submitBtn.disabled = false;
    }
});

// --- FILTER & LOOKUP ---

document.getElementById("applyFilterBtn").addEventListener("click", () => {
    loadPets(document.getElementById("speciesFilter").value.trim()).catch(err => showBanner(err.message, true));
});

document.getElementById("clearFilterBtn").addEventListener("click", () => {
    document.getElementById("speciesFilter").value = "";
    loadPets("").catch(err => showBanner(err.message, true));
});

document.getElementById("lookupPetBtn").addEventListener("click", async () => {
    const id = document.getElementById("lookupPetId").value;
    if (!id) return;
    try {
        const pet = await request(`/api/pets/${id}`);
        showBanner(`Found Pet #${pet.id}: ${pet.name} (${pet.species}, Status: ${pet.status})`);
    } catch (err) {
        showBanner(`Lookup failed (${err.status || "Network"}): ${err.message}`, true);
    }
});

// Initial load
refreshAll();