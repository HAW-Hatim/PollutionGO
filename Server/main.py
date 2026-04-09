from fastapi import FastAPI, Query
from fastapi import Body, HTTPException
from pydantic import BaseModel
from datetime import date, time
from sqlalchemy import create_engine, text
from fastapi import HTTPException
import geojson
import json
from typing import List



app = FastAPI()

# MariaDB-Verbindung
DATABASE_URL = "mysql+pymysql://fastapiuser:Air1234@localhost/PollutionDB"
engine = create_engine(DATABASE_URL)

# Datenmodell fÃ¼r FastAPI mit date und time
class Messwert(BaseModel):
    id: int 
    datum: date
    zeit: time
    breitengrad: float
    laengengrad: float
    pm1_0: float
    pm2_5: float
    pm10: float
    co2: float
    temperatur: float
    luftdruck: float
    luftfeuchtigkeit: float
    
class MesswertIn(BaseModel):
    datum: date
    zeit: time
    breitengrad: float
    laengengrad: float
    pm1_0: float
    pm2_5: float
    pm10: float
    co2: float
    temperatur: float
    luftdruck: float
    luftfeuchtigkeit: float
    

# Root-Endpunkt
@app.get("/")
def read_root():
    return {"message": "Hello World from Raspberry Pi!"}



@app.get("/download")
def get_geo(
    lat_min: float | None = Query(None),
    lat_max: float | None = Query(None),
    lon_min: float | None = Query(None),
    lon_max: float | None = Query(None),
    start_date: date | None = Query(None),
    start_time: time | None = Query(None),
    end_date: date | None = Query(None),
    end_time: time | None = Query(None)
):
    sql = """
        SELECT
            id,
            datum,
            zeit,
            breitengrad,
            laengengrad,
            pm1_0,
            pm2_5,
            pm10,
            co2,
            temperatur,
            luftdruck,
            luftfeuchtigkeit
        FROM Messwerte
        WHERE 1=1
    """
    params = {}

    if lat_min is not None and lat_max is not None:
        sql += " AND breitengrad BETWEEN :lat_min AND :lat_max"
        params["lat_min"] = lat_min
        params["lat_max"] = lat_max

    if lon_min is not None and lon_max is not None:
        sql += " AND laengengrad BETWEEN :lon_min AND :lon_max"
        params["lon_min"] = lon_min
        params["lon_max"] = lon_max

    if start_date is not None:
        if start_time is not None:
            sql += " AND (datum > :start_date OR (datum = :start_date AND zeit >= :start_time))"
            params["start_date"] = start_date
            params["start_time"] = start_time
        else:
            sql += " AND datum >= :start_date"
            params["start_date"] = start_date
    elif start_time is not None:
        sql += " AND zeit >= :start_time"
        params["start_time"] = start_time

    if end_date is not None:
        if end_time is not None:
            sql += " AND (datum < :end_date OR (datum = :end_date AND zeit <= :end_time))"
            params["end_date"] = end_date
            params["end_time"] = end_time
        else:
            sql += " AND datum <= :end_date"
            params["end_date"] = end_date
    elif end_time is not None:
        sql += " AND zeit <= :end_time"
        params["end_time"] = end_time

    features = []

    with engine.connect() as conn:
        rows = conn.execute(text(sql), params).mappings()

        for r in rows:
            geometry = geojson.Point((
                float(r["laengengrad"]),
                float(r["breitengrad"])
            ))

            properties = {
                "id": r["id"],
                "datum": str(r["datum"]),
                "zeit": str(r["zeit"]),
                "pm1_0": r["pm1_0"],
                "pm2_5": r["pm2_5"],
                "pm10": r["pm10"],
                "co2": r["co2"],
                "temperatur": r["temperatur"],
                "luftdruck": r["luftdruck"],
                "luftfeuchtigkeit": r["luftfeuchtigkeit"]
            }

            feature = geojson.Feature(
                geometry=geometry,
                properties=properties
            )
            features.append(feature)

    return geojson.FeatureCollection(features)
    
    
    
    


@app.post("/upload")
def create_messwert(m: MesswertIn):
    sql = """
        INSERT INTO Messwerte (
            datum, zeit, breitengrad, laengengrad,
            pm1_0, pm2_5, pm10,
            co2, temperatur, luftdruck, luftfeuchtigkeit
        )
        VALUES (
            :datum, :zeit, :breitengrad, :laengengrad,
            :pm1_0, :pm2_5, :pm10,
            :co2, :temperatur, :luftdruck, :luftfeuchtigkeit
        )
    """

    try:
        with engine.begin() as conn:
            conn.execute(text(sql), m.dict())
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    return {"status": "ok"}
    

