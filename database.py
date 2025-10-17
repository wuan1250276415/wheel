from sqlalchemy import create_engine, Column, Integer, String, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from datetime import datetime

SQLALCHEMY_DATABASE_URL = "sqlite:///./wheel_game.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


class SpinRecord(Base):
    """转盘记录表"""
    __tablename__ = "spin_records"

    id = Column(Integer, primary_key=True, index=True)
    player_name = Column(String, index=True)  # 玩家名字
    result = Column(String)  # 转盘结果
    result_type = Column(String)  # 类型: reward(奖励) 或 punishment(惩罚)
    created_at = Column(DateTime, default=datetime.now)  # 创建时间


class WheelOption(Base):
    """转盘选项表"""
    __tablename__ = "wheel_options"

    id = Column(Integer, primary_key=True, index=True)
    text = Column(String)  # 选项文字
    type = Column(String)  # reward 或 punishment
    emoji = Column(String)  # emoji 表情
    color = Column(String)  # 颜色
    is_active = Column(Integer, default=1)  # 是否启用


def init_db():
    """初始化数据库"""
    Base.metadata.create_all(bind=engine)

    # 添加默认选项
    db = SessionLocal()

    # 检查是否已有数据
    if db.query(WheelOption).count() == 0:
        default_options = [
            # 奖励
            WheelOption(text="亲亲抱抱", type="reward", emoji="💕", color="#FFB6C1"),
            WheelOption(text="按摩15分钟", type="reward", emoji="💆", color="#FFC0CB"),
            WheelOption(text="做喜欢的饭", type="reward", emoji="🍜", color="#FFD4E5"),
            WheelOption(text="看对方选的电影", type="reward", emoji="🎬", color="#FFE4E1"),
            WheelOption(text="免做家务一天", type="reward", emoji="🎉", color="#FFF0F5"),
            WheelOption(text="买小礼物", type="reward", emoji="🎁", color="#FFEBF0"),
            # 惩罚
            WheelOption(text="做家务一周", type="punishment", emoji="🧹", color="#E6E6FA"),
            WheelOption(text="洗碗三天", type="punishment", emoji="🍽️", color="#D8BFD8"),
            WheelOption(text="唱一首歌", type="punishment", emoji="🎤", color="#DDA0DD"),
            WheelOption(text="跳一支舞", type="punishment", emoji="💃", color="#EE82EE"),
            WheelOption(text="做50个深蹲", type="punishment", emoji="🏃", color="#DA70D6"),
            WheelOption(text="讲一个笑话", type="punishment", emoji="😄", color="#BA55D3"),
        ]
        db.add_all(default_options)
        db.commit()

    db.close()


def get_db():
    """获取数据库会话"""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
