/**
 * 通用响应结构定义
 */

/**
 * 统一响应结果
 * {
 *   code: 200,
 *   message: "请求成功",
 *   data: {...},
 *   timestamp: 123456789
 * }
 */
export interface Result<T = any> {
    code: number
    message: string
    data: T
    timestamp: number
}

/**
 * 分页主要数据
 * {
 *   current: 1,
 *   size: 20,
 *   total: 100,
 *   pages: 5,
 *   records: [...]
 * }
 */
export interface PageResult<T = any> {
    current: number
    size: number
    total: number
    pages: number
    records: T[]
}

/**
 * 错误码枚举
 */
export enum ErrorCode {
    SUCCESS = 200,
    BAD_REQUEST = 400,
    UNAUTHORIZED = 401,
    FORBIDDEN = 403,
    NOT_FOUND = 404,
    METHOD_NOT_ALLOWED = 405,
    REQUEST_TIMEOUT = 408,
    CONFLICT = 409,
    TOO_MANY_REQUESTS = 429,
    INTERNAL_SERVER_ERROR = 500,
    SERVICE_UNAVAILABLE = 503,
    GATEWAY_TIMEOUT = 504,

    // 10xx 通用业务错误
    OPERATION_FAILED = 1000,
    PARAM_VALIDATION_FAILED = 1001,
    PARAM_NOT_NULL = 1002,
    PARAM_FORMAT_ERROR = 1003,
    PARAM_OUT_OF_RANGE = 1004,
    DATA_NOT_FOUND = 1010,
    DATA_ALREADY_EXISTS = 1011,
    DATA_STATUS_ERROR = 1012,
    DATA_DELETED = 1013,
    RESOURCE_NOT_FOUND = 1014,
    RESOURCE_ALREADY_EXISTS = 1015,
    BUSINESS_RULE_VIOLATION = 1020,
    CONCURRENT_CONFLICT = 1021,
    IDEMPOTENT_CHECK_FAILED = 1022,
    CONFIG_ERROR = 1030,
    EXTERNAL_SERVICE_ERROR = 1040,
    NETWORK_ERROR = 1041,
    THIRD_PARTY_API_ERROR = 1042,
    SERIALIZATION_ERROR = 1050,
    DESERIALIZATION_ERROR = 1051,
    JSON_PARSE_ERROR = 1052,
    OPTIMISTIC_LOCK_CONFLICT = 1053,

    // 20xx 认证授权错误
    TOKEN_EXPIRED = 2001,
    TOKEN_INVALID = 2002,
    TOKEN_MISSING = 2003,
    TOKEN_BLACKLISTED = 2004,
    REFRESH_TOKEN_EXPIRED = 2005,
    REFRESH_TOKEN_INVALID = 2006,
    REFRESH_TOKEN_MISSING = 2007,

    // 40xx 租户错误
    TENANT_NOT_FOUND = 4001,
    TENANT_DISABLED = 4002,
    TENANT_EXPIRED = 4003,

    // 50xx 文件服务错误
    FILE_NOT_FOUND = 5001,
    FILE_TOO_LARGE = 5002,
    FILE_TYPE_NOT_SUPPORTED = 5003,
    FILE_UPLOAD_FAILED = 5004,
    FILE_DOWNLOAD_FAILED = 5005,
    STORAGE_SERVICE_ERROR = 5006,
    FILE_NAME_NOT_NULL = 5007,
    FILE_DELETE_FAILED = 5008
}
