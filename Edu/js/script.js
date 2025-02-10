document.getElementById('contactForm').addEventListener('submit', function (event) {
    event.preventDefault();

    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const message = document.getElementById('message').value;

    // Simulate sending an email (requires backend integration)
    console.log(`Sending email to ${email} and admin@example.com`);
    console.log(`Message: ${message}`);

    alert('Thank you for contacting us! We will get back to you soon.');
    document.getElementById('contactForm').reset();
});