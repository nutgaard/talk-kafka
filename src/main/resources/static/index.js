const input = document.getElementById('input');
const output = document.getElementById('output');
const ws = new WebSocket('ws://localhost:8080/kafka/ws');

const actionmap = {
    send(data) {
        repeat(data.count, (i) => {
            const message = replaceVars(input.value, {
                RND: Math.random().toString().slice(2)
            });
            ws.send(message);
        });
    }
}

ws.addEventListener('message', (event) => {
    console.log('data', event.data);
    output.value =  event.data + '\n\n' + output.value;
});

function repeat(n, fn) {
    for (let i = 0; i < n; i++) {
        fn(i + 1);
    }
}
function replaceVars(template, values) {
    const vars = Object.keys(values).join('|');
    const pattern = new RegExp(`\\{\\{\\s?(${vars})\\s?\\}\\}`, 'g');
    return template.replace(pattern, (_, match) => values[match]);
}

function setup() {
    input.value = JSON.stringify({
        id: "{{RND}}",
        content: "Some content: {{RND}}"
    }, null, 2);

    document.body.addEventListener('click', (event) => {
        if (event.target.tagName !== 'BUTTON') {
            return;
        }
        event.preventDefault();
        const target = event.target;
        const action = actionmap[target.dataset.action];
        if (action) {
            action(target.dataset);
        } else {
            alert('unknown action: ' + target.dataset.action);
        }
    });
}

setup();
