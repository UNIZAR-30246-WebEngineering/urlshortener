let limitActive = false;
let uri = "";

$(document).ready(
    function () {
        $("#server-response-error").hide()
        $("#server-response").hide()
        $("#limit-redirections").hide()
        // Uncheck checkbox on load
        $(':checkbox:checked').prop('checked',false);

        // Reset url field
        document.getElementById('shortener').reset()

        // Define popup clipboard msj
        var popup = document.createElement("div");
        var text = document.createTextNode("Copied to clipboard!");
        popup.appendChild(text);
        popup.style.visibility = "hidden";
        popup.id = "pop";

        $("#shortener").submit(
            async function (event) {
                $("#server-response-error").hide()
                $("#server-response").hide()

                event.preventDefault();
                let formData = $(this).serializeArray()

                const coords = await getCoords();
                if (coords !== undefined) {
                    formData.push({name: "lat", value: coords.lat})
                    formData.push({name: "lon", value: coords.lon})
                }

                if (!limitActive) {
                    formData[1].value = 0
                }

                formData.push({name: "qr", value: "true"})
                console.log(formData)

                $.ajax({
                    type: "POST",
                    url: "/api/link",
                    data: $.param(formData),
                    success: function (data, status) {
                        uri = data['url']
                        let chunks = data['url'].split('/');
                        $("#result").html(
                            "<a id='result' class='font-semibold text-blue-800' href='"
                            + data['url']
                            + "' target='_blank' rel='noreferrer'>"
                            + chunks[2] + "/" + chunks[3]
                            + "</a>");
                        $("#result2").html(
                            "<p id='result' class='w-72 truncate text-sm text-gray-500' style='width: 100%'>"
                            + formData[0].value
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

                        // Append popup for clipboard msj
                        document.getElementById("clipboard").appendChild(popup);
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

/**
 * Toggle limit redirections checkbox
 */
function toggleStatus() {
    if (limitActive) {
        limitActive = false;
        $("#limit-redirections").hide()
    } else {
        limitActive = true;
        $("#limit-redirections").show()
    }
}

/**
 * Copy link to clipboard
 */
function copy() {
    var pop = document.getElementById("pop");
    pop.style.visibility = "visible";
    window.setTimeout(
        function(){
                    document.getElementById("pop").style.visibility="hidden";
                },2000);

    // Copy the text inside the text field
    navigator.clipboard.writeText(uri).then(r => {
        // Success!
        console.log("Copied to clipboard");
    });
}

/**
 * Get coordinates of user
 * @returns {Promise<{lon: number, lat: number}>}
 */
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
