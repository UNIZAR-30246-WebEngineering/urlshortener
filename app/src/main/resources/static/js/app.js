let active = false;
let uri = "";

$(document).ready(
    function () {
        $("#server-response-error").hide()
        $("#server-response").hide()
        $("#limit-redirections").hide()
        active = false;

        $("#shortener").submit(
            async function (event) {
                $("#server-response-error").hide()
                $("#server-response").hide()

                event.preventDefault();
                let data = $(this).serializeArray()
                data[1] = {name: "limit", value : isNaN(parseInt(data[1]['value'])) ? 0 : parseInt(data[1]['value']) }

                console.log("Data : ", data)

                const coords = await getCoords();
                if (coords !== undefined) {
                    data.push({name: "lat", value: coords.lat})
                    data.push({name: "lon", value: coords.lon})
                }

                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: $.param(data),
                    success: function (data, status) {
                        uri = data['url']
                        let chunks = data['url'].split('/');
                        $("#result").html(
                            "<a id='result' class='font-semibold text-blue-800' href='"
                            + data['url']
                            + "' target='_blank' rel='noreferrer'>"
                            + chunks[3]
                            + "</a>");
                        $("#result2").html(
                            "<p id='result' class='w-72 truncate text-sm text-gray-500'>"
                            + data['url']
                            + "</p>");
                        $("#qr").html(
                            "<button class='group rounded-full bg-gray-100 p-1.5 transition-all duration-75 hover:scale-105 hover:bg-blue-100 active:scale-95' onClick='javascript:window.location=\""
                            + data['url'] + "/qr" + "\"'>"
                            + "<svg xmlns='http://www.w3.org/2000/svg' width='1em' height='1em' preserveAspectRatio='xMidYMid meet' viewBox='0 0 32 32' className='text-gray-700 transition-all group-hover:text-blue-800'>"
                            + "<path fill='currentColor' d='M24 28v-2h2v2zm-6-4v-2h2v2zm0 6h4v-2h-2v-2h-2v4zm8-4v-4h2v4zm2 0h2v4h-4v-2h2v-2zm-2-6v-2h4v4h-2v-2h-2zm-2 0h-2v4h-2v2h4v-6zm-6 0v-2h4v2zM6 22h4v4H6z'></path>"
                            + "<path fill='currentColor' d='M14 30H2V18h12zM4 28h8v-8H4zM22 6h4v4h-4z'></path>"
                            + "<path fill='currentColor' d='M30 14H18V2h12zm-10-2h8V4h-8zM6 6h4v4H6z'></path>"
                            + "<path fill='currentColor' d='M14 14H2V2h12ZM4 12h8V4H4Z'></path>"
                            + "</svg>"
                            + "</button>"
                        );

                        $("#server-response").show()
                    },
                    error: function (response, status) {
                        $("#error").html(
                            "<div class='alert alert-danger lead'>"
                            + "ERROR:" + "<br>"
                            + response['responseJSON']['message']
                            + "</div>");
                        $("#server-response-error").show()
                    }
                });
            });
    });

function toggleStatus() {
    if (active) {
        active = false;
        $("#limit-redirections").hide()
    } else {
        active = true;
        $("#limit-redirections").show()
    }
}

function copy() {
    // Copy the text inside the text field
    navigator.clipboard.writeText(uri).then(r => {
        // Success!
        console.log("Copied to clipboard");
    });
}

const getCoords = async () => {
    const pos = await new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject);
    }).catch(e => {
        //Error
        console.error("Error: " + e);
    });
    // Allow pressed
    if (pos !== undefined) {
        return {
            lat: pos.coords.latitude,
            lon: pos.coords.longitude,
        }
    }
}
