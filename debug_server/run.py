from flask import Flask, request
from flask import jsonify
import base64
app = Flask(__name__)


counter = 0

@app.route('/', methods=["GET", "POST"])
def hello_world():
    global counter

    result = {
        "responses": [{
            "safeSearchAnnotation": {
                "adult": "VERY_LIKELY",
                "spoof": "VERY_LIKELY",
                "medical": "UNLIKELY",
                "violence": "UNLIKELY",
                "racy": "VERY_UNLIKELY",
            }
        }]
    }

    rq = request.get_json()
    for r in rq['requests']:
        counter+=1
        with open(str(counter) + ".png", "wb") as img:
            img.write(base64.b64decode(r['image']['content']))


    return jsonify(result)


if __name__ == '__main__':
    app.run(host= '0.0.0.0')
