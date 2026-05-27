import request from "../request";

/**
 * 上传文件到 OSS
 * @param file 文件对象
 * @param dir 存储目录，默认 'common'（可选：'avatar', 'article' 等）
 * @returns OSS 文件 URL
 */
export function uploadFile(
  file: File,
  dir: string = "common",
): Promise<{ data: { code: number; message: string; data: string } }> {
  const formData = new FormData();
  formData.append("file", file);
  return request.post("/v1/admin/upload", formData, {
    params: { dir },
    headers: { "Content-Type": "multipart/form-data" },
  });
}
