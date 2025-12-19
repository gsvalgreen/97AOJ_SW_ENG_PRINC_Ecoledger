import api from './api';

export interface MovimentacaoRequest {
  producerId: string;
  commodityId: string;
  tipo: string;
  quantidade: number;
  unidade: string;
  timestamp: string; // ISO-8601 format
  localizacao?: {
    lat: number;
    lon: number;
  };
  anexos?: Array<{
    tipo: string;
    url: string;
    hash: string;
  }>;
}

export interface MovimentacaoResponse {
  movimentacaoId: string;
}

export interface MovimentacaoDetailResponse {
  id: string;
  producerId: string;
  commodityId: string;
  tipo: string;
  quantidade: number;
  unidade: string;
  timestamp: string;
  localizacao?: {
    lat: number;
    lon: number;
  };
  criadoEm: string;
  anexos: Array<{
    tipo: string;
    url: string;
    hash: string;
  }>;
}

export interface MovimentacaoListResponse {
  items: MovimentacaoDetailResponse[];
  total: number;
}

export interface UploadUrlResponse {
  uploadUrl: string;
  attachmentKey: string;
}

export interface ConfirmUploadRequest {
  attachmentKey: string;
}

export interface HistoricoMovimentacaoResponse {
  items: MovimentacaoDetailResponse[];
}

export const movimentacaoService = {
  criar: async (dados: MovimentacaoRequest, idempotencyKey?: string): Promise<MovimentacaoResponse> => {
    const headers: Record<string, string> = {};
    if (idempotencyKey) {
      headers['X-Idempotency-Key'] = idempotencyKey;
    }
    const response = await api.post<MovimentacaoResponse>('/movimentacoes', dados, { headers });
    return response.data;
  },

  buscarPorId: async (id: string): Promise<MovimentacaoDetailResponse> => {
    const response = await api.get<MovimentacaoDetailResponse>(`/movimentacoes/${id}`);
    return response.data;
  },

  listarPorProdutor: async (
    producerId: string,
    page: number = 1,
    size: number = 20,
    commodityId?: string,
    fromDate?: string,
    toDate?: string
  ): Promise<MovimentacaoListResponse> => {
    const params: any = { page, size };
    if (commodityId) params.commodityId = commodityId;
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;
    
    const response = await api.get<MovimentacaoListResponse>(`/produtores/${producerId}/movimentacoes`, { params });
    return response.data;
  },

  historicoPorCommodity: async (commodityId: string): Promise<HistoricoMovimentacaoResponse> => {
    const response = await api.get<HistoricoMovimentacaoResponse>(`/commodities/${commodityId}/historico`);
    return response.data;
  },

  getUploadUrl: async (fileName: string, contentType: string): Promise<UploadUrlResponse> => {
    const response = await api.post<UploadUrlResponse>('/anexos/upload-url', { fileName, contentType });
    return response.data;
  },

  confirmUpload: async (attachmentKey: string): Promise<void> => {
    await api.post('/anexos/confirm', { attachmentKey });
  },
};
