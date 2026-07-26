export interface PhotoDTO {
  filename: string;
  url: string;           // Imagem Original (HD)
  thumbnailUrl: string;  // Versão Otimizada (300x300px)
  uploadedAt: string;
  type: string;
  mimeType: string
}
