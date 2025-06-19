from flask import Flask, render_template, request, Response, jsonify
from flask_cors import CORS
import pandas as pd
import numpy as np
import os, json, urllib.parse, io, sys, unicodedata, re

sys.stdout = io.TextIOWrapper(sys.stdout.detach(), encoding="utf-8")

# ✅ 여기 수정만 핵심
current_path = os.path.dirname(os.path.abspath(__file__))  # 현재 py 폴더 (D:/.../py)
base_dir = os.path.abspath(os.path.join(current_path, '../'))  # python 폴더로 2단계 올라감
template_dir = os.path.join(base_dir, "templates")
csv_dir = os.path.join(base_dir, "csv")
app = Flask(__name__, template_folder=template_dir)

CORS(app, origins=["http://localhost:5000","http://localhost:9090"])

@app.route('/')
def index():
    return render_template(
        'traffic/data_index.html'
    )

@app.route('/accident')
def index_accident():
    csv_path = os.path.join(csv_dir, "accident.csv")
    df = pd.read_csv(csv_path, encoding="utf-8", thousands=",")

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

    전체데이터 = {
        '사고건수': 사고건수,
        '사망자수': 사망자수,
        '중상자수': 중상자수,
        '부상자수': 부상자수
    }

    return render_template(
        'traffic/accident.html',
        years=years,
        전체데이터=전체데이터
    )

@app.route('/weather')
def index_weather():
    csv_path = os.path.join(csv_dir, "weather.csv")
    df = pd.read_csv(csv_path, encoding="utf-8", thousands=",")

    df.columns = df.columns.str.strip()
    df = df.rename(columns={"자치구별(2)": "자치구", "항목": "기상상태"})
    df["자치구"] = df["자치구"].str.strip()
    df["기상상태"] = df["기상상태"].str.strip()

    weather_labels = ["맑음", "흐림", "비", "안개", "눈", "기타/불명"]
    years = ['2018', '2019', '2020', '2021', '2022', '2023']
    gu_list = df["자치구"].unique().tolist()
    gu_list = [gu for gu in gu_list if gu not in ['자치구별(2)']]  # 잘못된 헤더 제거

    chart_data = {}

    df_filtered = df[df["기상상태"].isin(["발생건수 (건)", "사망자 (명)", "부상자 (명)"])]

    for gu in gu_list:
        gu_data = df_filtered[df_filtered["자치구"] == gu]
        chart_data[gu] = {}

        for year in years:
            labels = [year] + [f"{year}.{i}" for i in range(1, 6)]
            year_data = {}

            for 항목 in ["발생건수 (건)", "사망자 (명)", "부상자 (명)"]:
                row = gu_data[gu_data["기상상태"] == 항목]
                values = []
                for col in labels:
                    val = row[col].values[0] if col in row else "0"
                    try:
                        num = int(str(val).replace("-", "0").strip())
                    except:
                        num = 0
                    values.append(num)
                year_data[항목] = values

            chart_data[gu][year] = year_data

    return render_template(
        'traffic/weather.html',
        years=years,
        labels=weather_labels,
        regions=gu_list,
        chart_data=chart_data
    )


# ✅ 안전한 정수 변환 함수 정의
def safe_int(val):
    try:
        return int(str(val).replace(',', '').strip())
    except:
        return 0

# CSV 불러오기 (다중 헤더)
csv_path = os.path.join(csv_dir, "roadtype.csv")
df = pd.read_csv(csv_path, encoding="utf-8", header=[0, 1])


# ❗ 다중 헤더 병합 먼저 수행
if isinstance(df.columns, pd.MultiIndex):
    df.columns = ['_'.join(col).replace(' ', '').replace('__', '_').strip('_') for col in df.columns]
else:
    df.columns = [col.strip() for col in df.columns]

# ✅ 그런 다음 컬럼 접근 가능
df['시군구_시군구'] = df['시군구_시군구'].astype(str).str.replace(r'\s+', '', regex=True)
df['사고년도_사고년도'] = df['사고년도_사고년도'].astype(str).str.replace(r'\s+', '', regex=True).str.strip()
    
# 연도 및 도로 형태
years = ['2018', '2019', '2020', '2021', '2022', '2023']

road_type_set = set()
for col in df.columns:
    if re.match(r"20\d{2}_.+", col):
        road_type_set.add(col.split('_', 1)[1])

road_types = sorted(road_type_set)

districts = df['시군구_시군구'].unique().tolist()

# 데이터 딕셔너리 구성
data_dict = {}
for district in districts:
    data_dict[district] = {}
    for year in years:
        try:
            # 현재 연도의 도로 컬럼만 선택
            cols = [f"{year}_{r}" for r in road_types]
            valid_cols = [col for col in cols if col in df.columns]
            if not valid_cols:
                print(f"{district}, {year}: 유효한 도로 타입 컬럼 없음")
                continue

            acc_row = df[(df['시군구_시군구'] == district) & (df['사고년도_사고년도'] == '사고[건]')]
            dea_row = df[(df['시군구_시군구'] == district) & (df['사고년도_사고년도'] == '사망[명]')]
            inj_row = df[(df['시군구_시군구'] == district) & (df['사고년도_사고년도'] == '부상[명]')]

            if acc_row.empty or dea_row.empty or inj_row.empty:
                print(f"{district}, {year}: 필요한 행 없음")
                continue

            acc = [safe_int(v) for v in acc_row[valid_cols].values[0]]
            dea = [safe_int(v) for v in dea_row[valid_cols].values[0]]
            inj = [safe_int(v) for v in inj_row[valid_cols].values[0]]

            data_dict[district][year] = {
                '사고건수': acc,
                '사망자수': dea,
                '부상자수': inj
            }

        except Exception as e:
            print(f"{district}, {year} 처리 중 오류 발생: {e}")
            continue

@app.route('/roadtype')
def index_roadtype():
    return render_template('traffic/roadtype.html', 
                           districts=districts,
                           years=['2018', '2019', '2020', '2021', '2022', '2023'],  # ✅ 이 부분,
                           road_types=road_types)

@app.route('/data/roadtype/<district>/<year>')
def roadtype_data(district, year):
    decoded_district = urllib.parse.unquote(district, encoding='utf-8')

    try:
        if decoded_district not in data_dict:
            print(f"❌ '{decoded_district}' 지역이 존재하지 않습니다.")
        elif year not in data_dict[decoded_district]:
            print(f"❌ '{decoded_district}'에 해당하는 연도 {year} 데이터가 없습니다.")

        data = data_dict.get(decoded_district, {}).get(year, {
            '사고건수': [], '사망자수': [], '부상자수': []
        })

        # 안전 정수 변환
        clean_data = {k: [safe_int(x) for x in v] for k, v in data.items()}
        print(f"[반환 데이터]: {decoded_district}, {year} → {clean_data}")

        return Response(
            json.dumps(clean_data, ensure_ascii=False),
            content_type='application/json; charset=utf-8'
        )

    except Exception as e:
        print(f"처리 중 오류: {e}")
        return Response(
            json.dumps({'error': str(e)}, ensure_ascii=False),
            content_type='application/json; charset=utf-8',
            status=500
        )


@app.route('/roadshape')
def index_roadshape():
    # CSV 파일 읽기 (헤더 3줄)
    csv_path = os.path.join(csv_dir, "roadshape.csv")
    df = pd.read_csv(csv_path, encoding="utf-8", header=[0, 1, 2], thousands=",")
    df.columns = ['_'.join([str(c) for c in col if pd.notna(c)]).strip() for col in df.columns]

    # 컬럼명 병합 후 유니코드 정규화
    df.columns = [
        unicodedata.normalize('NFC', '_'.join([str(c) for c in col if pd.notna(c)]).strip())
        for col in df.columns
    ]

    # 실제 컬럼명 확인

    # 컬럼 자동 이름 변경
    rename_map = {}
    for col in df.columns:
        if '사고년도' in col.replace('_', ''):
            rename_map[col] = '사고년도'
        elif '시도' in col.replace('_', ''):
            rename_map[col] = '시도'
        elif '시군구' in col.replace('_', ''):
            rename_map[col] = '시군구'

    df = df.rename(columns=rename_map)

    # 항목별 분리
    accident = df[df['사고년도'] == '사고[건]'].drop(columns=['사고년도'])
    death = df[df['사고년도'] == '사망[명]'].drop(columns=['사고년도'])
    injury = df[df['사고년도'] == '부상[명]'].drop(columns=['사고년도'])

    # melt
    accident_melted = accident.melt(id_vars=['시도', '시군구'], var_name='도로형태', value_name='사고건수')
    death_melted = death.melt(id_vars=['시도', '시군구'], var_name='도로형태', value_name='사망자수')
    injury_melted = injury.melt(id_vars=['시도', '시군구'], var_name='도로형태', value_name='부상자수')

    # 병합
    merged = accident_melted.merge(death_melted, on=['시도', '시군구', '도로형태'])
    merged = merged.merge(injury_melted, on=['시도', '시군구', '도로형태'])

    # 도로형태 → 연도/대분류/소분류 분리
    merged['연도'] = merged['도로형태'].apply(lambda x: x.split('_')[0])
    merged['대분류'] = merged['도로형태'].apply(lambda x: x.split('_')[1])
    merged['소분류'] = merged['도로형태'].apply(lambda x: x.split('_')[2])
    merged['도로형태'] = merged['대분류'] + '-' + merged['소분류']

    # 결측값 0으로 보정
    final = merged[['시도', '시군구', '연도', '도로형태', '사고건수', '부상자수', '사망자수']].fillna(0)

    years = sorted(final['연도'].unique())
    districts = sorted(final['시군구'].unique())

    # 연도+지역별 구조 생성
    data_nested = {}
    for year in years:
        data_nested[year] = {}
        for dist in districts:
            sub = final[(final['연도'] == year) & (final['시군구'] == dist)]
            if not sub.empty:
                data_nested[year][dist] = {
                    '도로형태': sub['도로형태'].tolist(),
                    '사고건수': sub['사고건수'].tolist(),
                    '부상자수': sub['부상자수'].tolist(),
                    '사망자수': sub['사망자수'].tolist()
            }

    return render_template('traffic/roadshape.html',
                           data=data_nested,
                           years=years,
                           districts=districts)


# CSV 파일 로딩
csv_path = os.path.join(csv_dir, "law_violation.csv")
df = pd.read_csv(csv_path, encoding="utf-8-sig", thousands=",", header=[0, 1])

# 다중헤더 → 문자열로 병합
if isinstance(df.columns, pd.MultiIndex):
    df.columns = ['_'.join(filter(None, col)).strip() for col in df.columns]

# 기본 설정
law_types = ['중앙선침범', '신호위반', '안전거리미확보', '안전운전의무불이행', '교차로운행방법위반', '보행자보호의무위반', '기타']
years = sorted({col[0] for col in df.columns if col[0].isdigit()})
cities = df['시도_시도'].unique()
districts_by_city = {city: df[df['시도_시도'] == city]['시군구_시군구'].unique() for city in cities}

@app.route("/violation/law")
def index_law_violation():
    csv_path = os.path.join(csv_dir, "law_violation.csv")
    df = pd.read_csv(csv_path, encoding="utf-8-sig", thousands=",", header=[0, 1])
    if isinstance(df.columns, pd.MultiIndex):
        df.columns = ['_'.join(filter(None, col)).strip() for col in df.columns]

    selected_city = cities[0]  # 시도 선택 (기본값: 첫 번째 도시)
    selected_district = request.args.get("district", districts_by_city[selected_city][0])
    selected_year = request.args.get("year", years[0])

    # 지역과 연도에 해당하는 행 추출
    region_df = df[(df['시도_시도'] == selected_city) & (df['시군구_시군구'] == selected_district)]

    try:
        accident_row = region_df[region_df['기준년도_기준년도'] == '사고[건]']
        death_row = region_df[region_df['기준년도_기준년도'] == '사망[명]']
        injury_row = region_df[region_df['기준년도_기준년도'] == '부상[명]']

# 데이터 리스트 초기화
        accident_data = []
        death_data = []
        injury_data = []

        for law in law_types:
            # 열 이름 생성
            col_name = f"{selected_year}_{law}"
            
            # 각 값 계산, 결측값 처리
            # .iloc[0]로 단일 값을 추출하고 Python float/int로 변환
            accident = float(accident_row[col_name].iloc[0]) if col_name in accident_row and not accident_row[col_name].isna().iloc[0] else 0.0
            death = float(death_row[col_name].iloc[0]) if col_name in death_row and not death_row[col_name].isna().iloc[0] else 0.0
            injury = float(injury_row[col_name].iloc[0]) if col_name in injury_row and not injury_row[col_name].isna().iloc[0] else 0.0

            # Python 기본 int로 변환
            accident_data.append(int(accident))
            death_data.append(int(death))
            injury_data.append(int(injury))

        # 리스트를 Python 기본 타입으로 한 번 더 보장
        accident_data = [int(x) for x in accident_data]
        death_data = [int(x) for x in death_data]
        injury_data = [int(x) for x in injury_data]

        return render_template(
            "traffic/law_violation.html",
            law_types=law_types,
            accident_data=accident_data,
            death_data=death_data,
            injury_data=injury_data,
            districts=list(districts_by_city[selected_city]),
            selected_district=str(selected_district),
            years=list(years),
            selected_year=str(selected_year)
        )

    except Exception as e:
            print(f"accident type: {type(accident_row[col_name].iloc[0])}")
            print(f"accident after float: {type(float(accident_row[col_name].iloc[0]))}")
            print(f"accident after int: {type(int(accident))}")
            print(f"accident_data: {accident_data}, type: {[type(x) for x in accident_data]}")
            return f"<h1>오류 발생:</h1><p>{e}</p>"


@app.route('/type')
def index_type():
    csv_path = os.path.join(csv_dir, "type.csv")
    df = pd.read_csv(csv_path, encoding="utf-8", thousands=",")

    # 복합헤더 구성
    year_row = df.iloc[0, 3:].tolist()
    type1_row = df.iloc[1, 3:].tolist()
    type2_row = df.iloc[2, 3:].tolist()
    multi_cols = pd.MultiIndex.from_arrays([year_row, type1_row, type2_row])

    # 실제 데이터
    df_data = df.iloc[3:].reset_index(drop=True)
    df_data.columns = ['시도', '시군구', '사고구분'] + list(multi_cols)

    # 숫자형 변환 함수
    def to_int(x):
        try:
            return int(str(x).replace(',', '').strip())
        except:
            return np.nan

    for col in df_data.columns[3:]:
        df_data[col] = df_data[col].apply(to_int)

    # 구, 연도, 사고유형 추출
    gu_list = sorted(df_data['시군구'].unique())
    years = sorted(set(col[0].split('.')[0] for col in df_data.columns[3:]))

    # 사고유형 필터링 (철길건널목, 도로이탈 제거)
    all_types = set(col[2] for col in df_data.columns[3:])
    제외유형 = {'철길건널목', '도로이탈'}
    types = sorted(all_types - 제외유형)

    # 전체 데이터 구성
    전체데이터 = {}
    for gu in gu_list:
        전체데이터[gu] = {}
        for year in years:
            year_data = {'사고건수': {}, '사망자수': {}, '부상자수': {}}
            for 구분 in ['사고[건]', '사망[명]', '부상[명]']:
                row = df_data[(df_data['시군구'] == gu) & (df_data['사고구분'] == 구분)]
                if row.empty:
                    continue
                for t in types:
                    cols = [col for col in df_data.columns[3:] if col[0].startswith(year) and col[2] == t]
                    avg = row[cols].mean(axis=1).values[0] if cols else 0
                    if 구분 == '사고[건]':
                        year_data['사고건수'][t] = round(avg, 2)
                    elif 구분 == '사망[명]':
                        year_data['사망자수'][t] = round(avg, 2)
                    elif 구분 == '부상[명]':
                        year_data['부상자수'][t] = round(avg, 2)
            전체데이터[gu][year] = year_data

    return render_template(
        'traffic/type.html',
        gu_list=gu_list,
        years=years,
        type_list=types,
        전체데이터=전체데이터
    )


# 다중 헤더 CSV 읽기
csv_path = os.path.join(csv_dir, "time.csv")
df = pd.read_csv(csv_path, encoding="utf-8", thousands=",")
df = df.iloc[1:]  # 첫 줄 제거

new_cols = []
for col in df.columns[3:]:
    parts = col.split('_')
    if len(parts) >= 2:
        new_cols.append(f"{parts[0]}_{parts[1]}")
    else:
        new_cols.append(col)  # 또는 continue

df.columns = ['시도', '시군구', '기준년도'] + new_cols


# 연도와 시간대 라벨 추출
years = sorted(set(col.split('_')[0] for col in df.columns[3:]))
time_slots = [col.split('_')[1] for col in df.columns[3:] if col.startswith('2018_')]  # 한 연도 분량 기준

# 구 목록
districts = [d for d in df['시군구'].unique().tolist() if isinstance(d, str) and all(ord(c) < 0x11000 for c in d)]

# 데이터 구성
data_dict = {}

for district in districts:
    if isinstance(district, str) and '�' in district:
        continue  # 무시하고 다음으로
    data_dict[district] = {}

    for year in years:
        acc_row = df[(df['시군구'] == district) & (df['기준년도'] == '사고[건]')]
        dea_row = df[(df['시군구'] == district) & (df['기준년도'] == '사망[명]')]
        inj_row = df[(df['시군구'] == district) & (df['기준년도'] == '부상[명]')]

        # ✅ 여기서 빈 데이터 방어 처리
        if acc_row.empty or dea_row.empty or inj_row.empty:
            data_dict[district][year] = {
                '사고건수': [0] * len(time_slots),
                '사망자수': [0] * len(time_slots),
                '부상자수': [0] * len(time_slots)
            }
            continue

        try:
            acc_data = acc_row[[f"{year}_{t}" for t in time_slots]].values[0].tolist()
            dea_data = dea_row[[f"{year}_{t}" for t in time_slots]].values[0].tolist()
            inj_data = inj_row[[f"{year}_{t}" for t in time_slots]].values[0].tolist()

            data_dict[district][year] = {
                '사고건수': list(map(int, acc_data)),
                '사망자수': list(map(int, dea_data)),
                '부상자수': list(map(int, inj_data))
            }
        except Exception as e:
            continue

@app.route('/time')
def index_time():
    return render_template('traffic/time.html',
                           districts=districts,
                           years=years,
                           time_labels=time_slots,
                           data_dict=data_dict)



@app.route('/age', methods=['GET'])
def index_age():
    region = request.args.get('region', '종로구')  # 기본 지역 설정

    csv_path = os.path.join(csv_dir, "age.csv")
    df = pd.read_csv(csv_path, encoding="utf-8", thousands=",")  # 단일 헤더이므로 header=[0, 1] 제거
    
    # 안전하게 접근
    df_region = df[df['시군구'] == region]

    accident_types = {
        '사고건수': '사고[건]',
        '사망자수': '사망[명]',
        '부상자수': '부상[명]'
    }

    age_labels = ['20세이하', '21~30세', '31~40세', '41~50세',
                  '51~60세', '61~64세', '65세이상', '연령불명']

    age_columns = [col for col in df.columns if any(str(y) in col for y in range(2018, 2024))]

    data = {}
    for key, val in accident_types.items():
        rows = df_region[df_region['사고년도'] == val]
        if rows.empty:
            data[key] = [0] * len(age_labels)
            continue

        averages = []
        for age in age_labels:
            cols_for_age = [col for col in age_columns if col.endswith(age)]
            values = rows[cols_for_age].values.flatten() if cols_for_age else []
            numeric_values = pd.to_numeric(values, errors='coerce')

            if numeric_values.size > 0:
                avg = round(np.nanmean(numeric_values), 2)
            else:
                avg = 0.0

            averages.append(avg)
        data[key] = averages

    return render_template('traffic/age.html', region=region, labels=age_labels,
                           사고건수=data['사고건수'], 사망자수=data['사망자수'], 부상자수=data['부상자수'])


# 데이터 로딩 및 전처리
csv_path = os.path.join(csv_dir, "weekday.csv")
df = pd.read_csv(csv_path, encoding="utf-8", thousands=",")

df.columns = df.columns.str.strip()  # 열 이름 공백 제거

# 요일 리스트 정의
weekday_order = ['일', '월', '화', '수', '목', '금', '토']
weekdays = df.iloc[1, 3:].tolist()  # '시도', '시군구', '사고년도' 제외 후 요일 리스트 추출

@app.route('/weekday')
def index_weekday():
    # 지역 목록 추출
    regions = df[df.iloc[:, 2] == '사고[건]']['시군구'].unique().tolist()
    return render_template('traffic/weekday.html', regions=regions)

@app.route('/data/weekday/<region>')
def index_weekday_data(region):
    사고 = df[(df['시군구'] == region) & (df['사고년도'] == '사고[건]')].iloc[:, 3:].sum().tolist()
    사망 = df[(df['시군구'] == region) & (df['사고년도'] == '사망[명]')].iloc[:, 3:].sum().tolist()
    부상 = df[(df['시군구'] == region) & (df['사고년도'] == '부상[명]')].iloc[:, 3:].sum().tolist()

    return jsonify({
        'labels': weekday_order,
        '사고건수': 사고[:7],
        '사망자수': 사망[:7],
        '부상자수': 부상[:7]
    })



@app.route('/monthly')
def index_monthly():
    return render_template('traffic/monthly.html')


@app.route('/data/<int:year>/<region>')
def index_get_region_data(year, region):
    # 다중 헤더 CSV 읽기
    csv_path = os.path.join(csv_dir, "monthly.csv")
    df = pd.read_csv(csv_path, header=[0, 1], encoding="utf-8", thousands=",")


    df.columns = pd.MultiIndex.from_tuples([
        (col[0], col[1]) if col[0] not in ['시도', '시군구', '사고년도'] else (col[0], '')
        for col in df.columns
    ])

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


@app.route('/daynight')
def index_daynight():
    # CSV 파일 읽기
    csv_path = os.path.join(csv_dir, "daynight.csv")
    df = pd.read_csv(csv_path, header=[0, 1], encoding="utf-8", thousands=",")

    # '사고[건]' 행만 선택
    df_accident = df[df[('사고년도', '사고년도')] == '사고[건]']

    # 연도 및 주/야 데이터 가져오기
    years = ['2018', '2019', '2020', '2021', '2022', '2023']
    day = []
    night = []

    for year in years:
        day_value = int(df_accident[(year, '주')].values[0])
        night_value = int(df_accident[(year, '야')].values[0])
        day.append(day_value)
        night.append(night_value)

    return render_template('traffic/daynight.html', years=years, day=day, night=night)



@app.route('/vehicle', methods=['GET'])
def index_vehicle():
    region = request.args.get('region', '종로구')
    selected_year = request.args.get('year', 'all')

    csv_path = os.path.join(csv_dir, "vehicle.csv")
    df = pd.read_csv(csv_path, header=[0, 1], encoding="utf-8")

    df.columns = [f"{a}_{b}" if b else a for a, b in df.columns]
    df = df[df['시군구_시군구'] == region]

    accident_types = {
        '사고건수': '사고[건]',
        '사망자수': '사망[명]',
        '부상자수': '부상[명]'
    }

    vehicle_labels = [
        '승용차', '승합차', '화물차', '특수차', '이륜차',
        '사륜오토바이(ATV)', '원동기장치자전거', '자전거',
        '개인형이동장치(PM)', '건설기계', '농기계', '기타/불명'
    ]

    year_columns = [col for col in df.columns if col.split('_')[0].isdigit()]
    data = {}

    for label, type_str in accident_types.items():
        rows = df[df['사고년도_사고년도'] == type_str]

        if rows.empty:
            data[label] = [0] * len(vehicle_labels)
            continue

        values = []
        for vehicle in vehicle_labels:
            if selected_year == 'all':
                cols = [col for col in year_columns if col.endswith(vehicle)]
            else:
                cols = [col for col in year_columns if col.startswith(selected_year) and col.endswith(vehicle)]

            v = rows[cols].values.flatten()
            v = pd.to_numeric(v, errors='coerce')

            if label == '사망자수':
                v = v[(v >= 0) & (v <= 20)]
            else:
                v = v[v >= 0]

            avg = round(np.nanmean(v), 2) if len(v) > 0 else 0
            values.append(avg)

        data[label] = values

    return render_template('traffic/vehicle.html',
                           region=region,
                           year=selected_year,
                           labels=vehicle_labels,
                           사고건수=data['사고건수'],
                           사망자수=data['사망자수'],
                           부상자수=data['부상자수']
                           )

if __name__ == '__main__':
    app.run(debug=True)