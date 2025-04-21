from flask import Flask, render_template
import pandas as pd

app = Flask(__name__)

@app.route('/')
def index():
    # CSV 로딩 (쉼표 숫자 자동 변환)
    df = pd.read_csv('교통사고 건.csv', thousands=',')

    # 서울만 추출
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

    return render_template(
        '교통사고 건.html',
        years=years,
        사고건수=사고건수,
        사망자수=사망자수,
        중상자수=중상자수,
        부상자수=부상자수
    )

if __name__ == '__main__':
    app.run(debug=True)
