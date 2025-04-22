from flask import Flask, render_template, jsonify
import pandas as pd

app = Flask(__name__)

@app.route('/')
def index():
    return render_template('monthly.html')


@app.route('/data/<int:year>/<region>')
def get_region_data(year, region):
    # 다중 헤더 CSV 읽기
    df = pd.read_csv("월별.csv", header=[0, 1], encoding="utf-8")
    
    # 필요한 메타컬럼만 수동 처리
    df.columns = pd.MultiIndex.from_tuples(
        [(col[0], col[1]) if col[0] not in ['시도', '시군구', '사고년도'] else (col[0], '') for col in df.columns]
    )

    # 시군구, 사고년도 등 정리
    df = df.rename(columns={('시도', ''): '시도', ('시군구', ''): '시군구', ('사고년도', ''): '사고년도'})
    
    # 선택한 지역 + 사고유형별 필터
    label_map = {
        "사고건수": "사고[건]",
        "사망자수": "사망[명]",
        "부상자수": "부상[명]"
    }

    result = {
        "labels": ["01월", "02월", "03월", "04월", "05월", "06월",
                   "07월", "08월", "09월", "10월", "11월", "12월"],
        "사고건수": [],
        "사망자수": [],
        "부상자수": []
    }

    for key, label in label_map.items():
        filtered = df[(df["시군구"] == region) & (df["사고년도"] == label)]

        if not filtered.empty:
            values = [int(filtered[(str(year), f"{month}")].values[0]) for month in result["labels"]]
            result[key] = values
        else:
            result[key] = [0] * 12

    return jsonify(result)


if __name__ == '__main__':
    app.run(debug=True)
