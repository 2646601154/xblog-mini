import request from "../request";
import type { PageResult } from "@/types";
import type { AxiosResponse } from "axios";

export interface CategoryVO {
  id: number;
  name: string;
  slug: string;
  description?: string;
}

export interface AuthorVO {
  id: number;
  username: string;
  nickname: string;
  avatar: string;
}

export interface TagVO {
  id: number;
  name: string;
  slug: string;
}

export interface ArticleListDTO {
  page?: number;
  size?: number;
  categoryId?: number;
  tagId?: number;
  keyword?: string;
}

export interface ArticleListItemVO {
  id: number;
  title: string;
  summary: string;
  coverImage: string;
  category: CategoryVO;
  author: AuthorVO;
  tags: TagVO[];
  viewCount: number;
  publishedAt: string;
  createdAt: string;
}

export type ArticleListVO = PageResult<ArticleListItemVO>;

export interface ArticleDetailVO {
  id: number;
  title: string;
  summary: string;
  content: string;
  coverImage: string;
  category: CategoryVO;
  author: AuthorVO;
  tags: TagVO[];
  status: string;
  viewCount: number;
  publishedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface ArticleBriefVO {
  id: number;
  title: string;
}

export interface ArticlePrevNextVO {
  previous: ArticleBriefVO | null;
  next: ArticleBriefVO | null;
}

export type ArticleListResponse = Promise<
  AxiosResponse<{ code: number; message: string; data: ArticleListVO }>
>;
export type ArticleDetailResponse = Promise<
  AxiosResponse<{ code: number; message: string; data: ArticleDetailVO }>
>;
export type ArticlePrevNextResponse = Promise<
  AxiosResponse<{ code: number; message: string; data: ArticlePrevNextVO }>
>;
export type CategoryListResponse = Promise<
  AxiosResponse<{ code: number; message: string; data: CategoryVO[] }>
>;
export type TagListResponse = Promise<
  AxiosResponse<{ code: number; message: string; data: TagVO[] }>
>;

export function getArticleList(params: ArticleListDTO): ArticleListResponse {
  return request.get("/v1/articles", { params });
}

export function getArticleDetail(id: number): ArticleDetailResponse {
  return request.get(`/v1/articles/${id}`);
}

export function getArticlePrevNext(id: number): ArticlePrevNextResponse {
  return request.get(`/v1/articles/${id}/prev-next`);
}
