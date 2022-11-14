function getData(event) {
    event.preventDefault();
    getURL(document.getElementsByName('url').item(0).value);
}

function getURL(url){
    const options = {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: new URLSearchParams({url})
    };
      
    fetch('http://localhost:8080/api/link', options)
        .then(response => response.json())
        .then(response => getQR(response.url))
        .catch(() =>
            document.getElementById('result').innerHTML =
                `<div class='alert alert-danger lead'>ERROR</div>`
        );
}

// TODO: IMAGE STYLE WITH BORDER WHITE

function getQR(content){
    var widthProp = "-webkit-fill-available"

    fetch(`http://localhost:8080/qrcode?content=${content}`)
        .then(response => response.blob())
        .then(blob => {
            var image = URL.createObjectURL(blob);
            document.getElementById('result').innerHTML = 
                `<div class='alert alert-success lead'>
                    <a target='_blank' href="${content}">${content}</a>
                    <br>
                    <img src="${image}" style="width: -webkit-fill-available;margin: 1rem 0; border-radius: 5%; border: 15px solid white"/>
                </div>`;
        })
        .catch(() =>
            document.getElementById('result').innerHTML =
                `<div class='alert alert-danger lead'>ERROR</div>`
        );
}

document
    .getElementById('shortener')
    .addEventListener('submit', getData);