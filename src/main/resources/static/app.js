const API_BASE = "http://localhost:8080";

async function getJson(path) {
    const response = await fetch(`${API_BASE}${path}`);
    if (response.status === 404) {
        throw new Error("Record not found (404).");
    }
    if (!response.ok) {
        throw new Error(`Request failed (${response.status})`);
    }
    return response.json();
}

async function loadPets() {
    const status = document.querySelector("#status");
    const list = document.querySelector("#items");
    list.replaceChildren();
    status.textContent = "Loading pets...";

    try {
        const rows = await getJson("/api/pets?page=0&size=10");

        if (!Array.isArray(rows)) {
            throw new Error("Unexpected list response format");
        }

        for (const pet of rows) {
            const li = document.createElement("li");
            li.textContent = `#${pet.id} - ${pet.name} (${pet.species} • ${pet.breed})`;
            li.addEventListener("click", () => loadPetDetail(pet.id));
            list.append(li);
        }

        status.textContent = rows.length ? "" : "No pets found in the database.";
    } catch (error) {
        status.textContent = error.message;
    }
}

async function loadPetDetail(id) {
    const detail = document.querySelector("#detail");
    detail.textContent = "Loading details...";

    try {
        const pet = await getJson(`/api/pets/${id}`);
        detail.replaceChildren();

        const fields = [
            ["ID", pet.id],
            ["Microchip Number", pet.microchipNumber],
            ["Name", pet.name],
            ["Species", pet.species],
            ["Breed", pet.breed]
        ];

        for (const [label, value] of fields) {
            const p = document.createElement("p");
            p.textContent = `${label}: ${value ?? "N/A"}`;
            detail.append(p);
        }
    } catch (error) {
        detail.textContent = error.message;
    }
}

document.querySelector("#reload-btn").addEventListener("click", loadPets);
document.querySelector("#lookup-btn").addEventListener("click", () => {
    const id = document.querySelector("#pet-id-input").value;
    if (id) loadPetDetail(id);
});

loadPets();