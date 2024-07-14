document.addEventListener('DOMContentLoaded', function () {
    const platformsContainer = document.getElementById('platforms');
    const showMenu = document.getElementById('show-menu');
    const outfitsContainer = document.getElementById('outfits');
    const dropdownBtn = document.querySelector('.dropbtn');

    const showsByPlatform = {
        netflix: [
            { img: 'images/friends.jpg', title: 'Friends' },
            { img: 'images/queens-gambit.jpg', title: 'The Queen\'s Gambit' },
            { img: 'images/emily-in-paris.jpg', title: 'Emily in Paris' }
        ],
        sony: [
            // Add shows for Sony Liv
        ],
        disney: [
            // Add shows for Disney+
        ]
    };

    const outfitsByShow = {
        'Friends': [
            { img: 'images/friends-outfit1.jpg', description: 'Friends Outfit 1' },
            { img: 'images/friends-outfit2.jpg', description: 'Friends Outfit 2' }
        ],
        'The Queen\'s Gambit': [
            { img: 'images/queens-gambit-outfit1.jpg', description: 'Queen\'s Gambit Outfit 1' },
            { img: 'images/queens-gambit-outfit2.jpg', description: 'Queen\'s Gambit Outfit 2' }
        ],
        'Emily in Paris': [
            { img: 'images/emily-in-paris-outfit1.jpg', description: 'Emily in Paris Outfit 1' },
            { img: 'images/emily-in-paris-outfit2.jpg', description: 'Emily in Paris Outfit 2' }
        ]
        // Add more shows and outfits as needed
    };

    platformsContainer.addEventListener('click', function (event) {
        if (event.target.tagName === 'IMG') {
            // Remove selected class from previous selected
            document.querySelectorAll('#platforms img').forEach(img => img.classList.remove('selected'));

            // Add selected class to clicked platform
            event.target.classList.add('selected');

            const selectedPlatform = event.target.getAttribute('data-platform');
            const shows = showsByPlatform[selectedPlatform];

            // Clear previous shows
            showMenu.innerHTML = '';

            // Add new shows
            shows.forEach(show => {
                const div = document.createElement('div');
                div.className = 'show-item';
                div.innerHTML = `
                    <img src="${show.img}" alt="${show.title}">
                    <span>${show.title}</span>
                `;
                div.addEventListener('click', function () {
                    displayOutfits(show.title);
                    dropdownBtn.textContent = show.title;
                    showMenu.classList.remove('show');
                });
                showMenu.appendChild(div);
            });

            // Clear outfits display
            outfitsContainer.innerHTML = '';
        }
    });

    dropdownBtn.addEventListener('click', function () {
        showMenu.classList.toggle('show');
    });

    function displayOutfits(showTitle) {
        const outfits = outfitsByShow[showTitle];

        // Clear previous outfits
        outfitsContainer.innerHTML = '';

        // Display new outfits
        outfits.forEach(outfit => {
            const div = document.createElement('div');
            div.className = 'outfit-item';
            div.innerHTML = `
                <img src="${outfit.img}" alt="${outfit.description}">
                <p>${outfit.description}</p>
            `;
            outfitsContainer.appendChild(div);
        });
    }

    // Close the dropdown if the user clicks outside of it
    window.addEventListener('click', function (event) {
        if (!event.target.matches('.dropbtn')) {
            const dropdowns = document.getElementsByClassName('dropdown-content');
            for (let i = 0; i < dropdowns.length; i++) {
                const openDropdown = dropdowns[i];
                if (openDropdown.classList.contains('show')) {
                    openDropdown.classList.remove('show');
                }
            }
        }
    });
});
