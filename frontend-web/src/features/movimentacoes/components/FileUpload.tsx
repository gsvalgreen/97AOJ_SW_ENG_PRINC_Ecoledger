import { useState, useCallback } from 'react';
import {
  Box,
  Button,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton,
  LinearProgress,
  Alert,
} from '@mui/material';
import { CloudUpload, Delete, AttachFile } from '@mui/icons-material';
import { movimentacoesApi } from '../../../api/movimentacoesApi';

export interface UploadedFile {
  objectKey: string;
  url: string;
  tipo: string;
  hash: string;
  size: number;
  file: File;
}

interface FileUploadProps {
  onFilesChange: (files: UploadedFile[]) => void;
  maxFiles?: number;
  acceptedTypes?: string[];
  maxSizeMB?: number;
}

const FileUpload = ({
  onFilesChange,
  maxFiles = 10,
  acceptedTypes = ['image/*', 'application/pdf'],
  maxSizeMB = 10,
}: FileUploadProps) => {
  const [files, setFiles] = useState<UploadedFile[]>([]);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFileSelect = useCallback(
    async (selectedFiles: FileList | null) => {
      if (!selectedFiles || selectedFiles.length === 0) return;

      setError(null);

      if (files.length + selectedFiles.length > maxFiles) {
        setError(`Máximo de ${maxFiles} arquivos permitidos`);
        return;
      }

      const filesArray = Array.from(selectedFiles);

      for (const file of filesArray) {
        if (file.size > maxSizeMB * 1024 * 1024) {
          setError(`Arquivo ${file.name} excede o tamanho máximo de ${maxSizeMB}MB`);
          continue;
        }

        const isAccepted = acceptedTypes.some((type) => {
          if (type.endsWith('/*')) {
            return file.type.startsWith(type.slice(0, -1));
          }
          return file.type === type;
        });

        if (!isAccepted) {
          setError(`Tipo de arquivo não permitido: ${file.name}`);
          continue;
        }

        try {
          setUploading(true);
          const { objectKey, uploadUrl } = await movimentacoesApi.gerarUploadUrl(file.type);

          const formData = new FormData();
          formData.append('file', file);

          const uploadResponse = await fetch(uploadUrl, {
            method: 'PUT',
            body: file,
            headers: {
              'Content-Type': file.type,
            },
          });

          if (!uploadResponse.ok) {
            throw new Error('Erro ao fazer upload do arquivo');
          }

          const confirmed = await movimentacoesApi.confirmarUpload(objectKey);

          const uploadedFile: UploadedFile = {
            ...confirmed,
            file,
          };

          setFiles((prev) => [...prev, uploadedFile]);
        } catch (err) {
          setError(err instanceof Error ? err.message : 'Erro ao fazer upload');
        } finally {
          setUploading(false);
        }
      }
    },
    [files.length, maxFiles, maxSizeMB, acceptedTypes]
  );

  const handleRemove = (index: number) => {
    const newFiles = files.filter((_, i) => i !== index);
    setFiles(newFiles);
    onFilesChange(newFiles);
  };

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      handleFileSelect(e.dataTransfer.files);
    },
    [handleFileSelect]
  );

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  return (
    <Box>
      <Paper
        elevation={2}
        sx={{
          p: 3,
          border: '2px dashed',
          borderColor: 'divider',
          textAlign: 'center',
          cursor: 'pointer',
          '&:hover': {
            borderColor: 'primary.main',
            backgroundColor: 'action.hover',
          },
        }}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
      >
        <input
          accept={acceptedTypes.join(',')}
          style={{ display: 'none' }}
          id="file-upload"
          multiple
          type="file"
          onChange={(e) => handleFileSelect(e.target.files)}
          disabled={uploading || files.length >= maxFiles}
        />
        <label htmlFor="file-upload">
          <Button
            variant="outlined"
            component="span"
            startIcon={<CloudUpload />}
            disabled={uploading || files.length >= maxFiles}
            sx={{ mb: 2 }}
          >
            Selecionar Arquivos
          </Button>
        </label>
        <Typography variant="body2" color="text.secondary">
          Arraste arquivos aqui ou clique para selecionar
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          Máximo {maxFiles} arquivos, {maxSizeMB}MB por arquivo
        </Typography>
      </Paper>

      {uploading && (
        <Box sx={{ mt: 2 }}>
          <LinearProgress />
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Fazendo upload...
          </Typography>
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {error}
        </Alert>
      )}

      {files.length > 0 && (
        <Box sx={{ mt: 2 }}>
          <Typography variant="subtitle2" gutterBottom>
            Arquivos anexados ({files.length})
          </Typography>
          <List>
            {files.map((file, index) => (
              <ListItem
                key={index}
                secondaryAction={
                  <IconButton edge="end" onClick={() => handleRemove(index)}>
                    <Delete />
                  </IconButton>
                }
              >
                <ListItemIcon>
                  <AttachFile />
                </ListItemIcon>
                <ListItemText
                  primary={file.file.name}
                  secondary={`${(file.size / 1024).toFixed(2)} KB - ${file.tipo}`}
                />
              </ListItem>
            ))}
          </List>
        </Box>
      )}
    </Box>
  );
};

export default FileUpload;

