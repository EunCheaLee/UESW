from flask import Flask, jsonify
import pandas as pd
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

@app.route('/carAccident', methods=['GET'])
def get_car_accident_data():
    df = pd.read_csv('교통사고 건.csv', thousands=',')

    df.columns = df.columns.str.strip()
    df['광역지자체'] = df['광역지자체'].str.strip()
    seoul = df[df['광역지자체'] == '서울']

    years = ['2018', '2019', '2020', '2021', '2022', '2023']

    def clean(val):
        try:
            return int(str(val).replace(',', '').strip())
        except:
            return 0

    사고건수 = [clean(seoul[year].values[0]) for year in years]
    사망자수 = [clean(seoul[f"{year}.1"].values[0]) for year in years]
    중상자수 = [clean(seoul[f"{year}.2"].values[0]) for year in years]
    부상자수 = [clean(seoul[f"{year}.3"].values[0]) for year in years]

    return jsonify({
        "years": years,
        "사고건수": 사고건수,
        "사망자수": 사망자수,
        "중상자수": 중상자수,
        "부상자수": 부상자수
    })

if __name__ == '__main__':
    app.run(port=5000, debug=True)