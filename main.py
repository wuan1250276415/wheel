from fastapi import FastAPI, Depends, HTTPException
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime
import random

from database import init_db, get_db, SpinRecord, WheelOption

app = FastAPI(title="转盘游戏 API")

# 初始化数据库
init_db()

# 挂载静态文件目录
app.mount("/static", StaticFiles(directory="static"), name="static")


# Pydantic 模型
class SpinRequest(BaseModel):
    player_name: str


class SpinResponse(BaseModel):
    id: int
    player_name: str
    result: str
    result_type: str
    emoji: str
    color: str
    created_at: datetime


class RecordResponse(BaseModel):
    id: int
    player_name: str
    result: str
    result_type: str
    created_at: datetime

    class Config:
        from_attributes = True


class WheelOptionResponse(BaseModel):
    id: int
    text: str
    type: str
    emoji: str
    color: str

    class Config:
        from_attributes = True


class StatsResponse(BaseModel):
    total_spins: int
    player_stats: dict
    result_distribution: dict


@app.get("/")
async def read_root():
    """返回主页"""
    return FileResponse("static/index.html")


@app.get("/api/options", response_model=List[WheelOptionResponse])
async def get_options(db: Session = Depends(get_db)):
    """获取所有转盘选项"""
    options = db.query(WheelOption).filter(WheelOption.is_active == 1).all()
    return options


@app.post("/api/spin", response_model=SpinResponse)
async def spin_wheel(request: SpinRequest, db: Session = Depends(get_db)):
    """转动转盘"""
    # 获取所有启用的选项
    options = db.query(WheelOption).filter(WheelOption.is_active == 1).all()

    if not options:
        raise HTTPException(status_code=404, detail="没有可用的转盘选项")

    # 随机选择一个结果
    selected = random.choice(options)

    # 保存记录
    record = SpinRecord(
        player_name=request.player_name,
        result=selected.text,
        result_type=selected.type
    )
    db.add(record)
    db.commit()
    db.refresh(record)

    return SpinResponse(
        id=record.id,
        player_name=record.player_name,
        result=record.result,
        result_type=record.result_type,
        emoji=selected.emoji,
        color=selected.color,
        created_at=record.created_at
    )


@app.get("/api/records", response_model=List[RecordResponse])
async def get_records(
    player_name: Optional[str] = None,
    limit: int = 50,
    db: Session = Depends(get_db)
):
    """获取历史记录"""
    query = db.query(SpinRecord)

    if player_name:
        query = query.filter(SpinRecord.player_name == player_name)

    records = query.order_by(SpinRecord.created_at.desc()).limit(limit).all()
    return records


@app.get("/api/stats", response_model=StatsResponse)
async def get_statistics(db: Session = Depends(get_db)):
    """获取统计信息"""
    records = db.query(SpinRecord).all()

    total_spins = len(records)

    # 玩家统计
    player_stats = {}
    for record in records:
        if record.player_name not in player_stats:
            player_stats[record.player_name] = {
                "total": 0,
                "rewards": 0,
                "punishments": 0
            }
        player_stats[record.player_name]["total"] += 1
        if record.result_type == "reward":
            player_stats[record.player_name]["rewards"] += 1
        else:
            player_stats[record.player_name]["punishments"] += 1

    # 结果分布
    result_distribution = {}
    for record in records:
        if record.result not in result_distribution:
            result_distribution[record.result] = 0
        result_distribution[record.result] += 1

    return StatsResponse(
        total_spins=total_spins,
        player_stats=player_stats,
        result_distribution=result_distribution
    )


@app.delete("/api/records/{record_id}")
async def delete_record(record_id: int, db: Session = Depends(get_db)):
    """删除记录"""
    record = db.query(SpinRecord).filter(SpinRecord.id == record_id).first()
    if not record:
        raise HTTPException(status_code=404, detail="记录不存在")

    db.delete(record)
    db.commit()
    return {"message": "删除成功"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
